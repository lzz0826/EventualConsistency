package org.example.service;

import static org.example.client.service.StockClientService.RepStock;

import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import lombok.extern.log4j.Log4j2;
import org.example.client.service.StockClientService;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
import org.example.controller.rep.CreateOrderContent;
import org.example.dao.OrderDao;
import org.example.dao.OrderStockMiddleDao;
import org.example.entities.Order;
import org.example.entities.Stock;
import org.example.entities.middle.OrderStockMiddle;
import org.example.enums.OrderStatusEnum;
import org.example.exception.AddOrderException;
import org.example.exception.AddOrderStockMiddleException;
import org.example.exception.DeductedStockQuantityException;
import org.example.exception.NoStockException;
import org.example.exception.NotFoundOrderException;
import org.example.exception.OkHttpGetException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Log4j2
@Service
public class OrderService {


  @Resource
  private StockClientService stockClientService;

  @Resource
  private OrderDao orderDao;

  @Resource
  private OrderStockMiddleDao orderStockMiddleDao;

  /**
   * 創建訂單 Seata 強一致
   **/
  // Transactional 第一入口加上GlobalTransactional 補償任務 Seata 會做
  // 原本的 @Transactional還是要加上
  @GlobalTransactional
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public boolean createOrder(String product_name, int quantity)
      throws OkHttpGetException, NoStockException, DeductedStockQuantityException, AddOrderException, AddOrderStockMiddleException {

    log.info("Seata全局事务id=================>{}", RootContext.getXID());

    BaseResp<Stock> stockByProductName = stockClientService.getStockByProductName(product_name);
    Stock stock = RepStock(stockByProductName);

    if (stock == null) {
      throw new NoStockException();
    }

    Long id = stock.getId();

    //請求Stock服務 扣庫存 回滾只會在Order上 Stock 需要處理
    //加上GlobalTransactional 分布式事務
    boolean deductedStockQuantity = stockClientService.deductedStockQuantity(String.valueOf(id),
        String.valueOf(quantity));

    if (!deductedStockQuantity) {

      throw new DeductedStockQuantityException();
    }
    creatOrderAndMiddle(stock,quantity);
    return true;
  }


  /**
   * 創建訂單 Mq 最終一致
   * 目前 一個產品一個訂單
   * 之後可以用List帶product_name 每筆訂單配一個product_name
   **/
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public boolean createOrderMq(Map<String,Integer> product_quantity)
          throws NoStockException, AddOrderException, AddOrderStockMiddleException, DeductedStockQuantityException {

    List<String> product_names = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : product_quantity.entrySet()) {
      product_names.add(entry.getKey());
    }
    //檢查庫存 打Stock服務
    List<Stock> stocks = stockClientService.getStockByProductNames(product_names);
    if (product_names.size() != stocks.size()){
      throw new NoStockException();
    }
    for (Stock stock : stocks) {
      //檢查查出的 stock 是否有對應
      String productName = stock.getProduct_name();
      if (!product_quantity.containsKey(productName)){
        throw new NoStockException();
      }
      //檢查庫存是否足夠
      Integer quantity = stock.getQuantity();
      if (quantity < product_quantity.get(productName)){
        throw new NoStockException();
      }
    }
    //產生訂單
    for (Stock stock : stocks) {
      creatOrderAndMiddle(stock,product_quantity.get(stock.getProduct_name()));
    }
    return true;
  }

  //TODO MQ發消息檢查訂單狀態 通知Stock庫存回滾
  public void creatOrderAndMiddle(Stock stock,int orderQuantity) throws AddOrderException, NoStockException, DeductedStockQuantityException,
          AddOrderStockMiddleException {
    Order order = Order
            .builder()
            .price(stock.getPrice().multiply(BigDecimal.valueOf(orderQuantity)))
            .type(1)
            .status(OrderStatusEnum.CreateIng.code)
            .create_time(new Date())
            .update_time(new Date())
            .build();
    Long addOrder = orderDao.addOrderRepId(order);
    if (addOrder == 0) {
      log.error(StatusCode.AddOrderFail.msg);
      throw new AddOrderException();
    }

    Long id = stock.getId();

    boolean deductedStockQuantity = stockClientService.deductedStockQuantityMq(String.valueOf(id),
            String.valueOf(order.getId()), String.valueOf(orderQuantity));

    if (!deductedStockQuantity) {
      throw new DeductedStockQuantityException();
    }
    //*如果沒有分布式事務 這邊報異常 Order會回滾(沒有天加訂單)
//    int sdf = 10/0;
    OrderStockMiddle orderStockMiddle = OrderStockMiddle
            .builder()
            .order_id(order.getId())
            .status(order.getStatus())
            .deducted_quantity(orderQuantity)
            .stock_id(stock.getId())
            .create_time(new Date())
            .update_time(new Date())
            .build();
    boolean addOrderStockMiddle = orderStockMiddleDao.addOrderStockMiddle(orderStockMiddle);
    if (!addOrderStockMiddle) {
      log.error(StatusCode.AddOrderStockMiddleFail.msg);
      throw new AddOrderStockMiddleException();
    }
  }


  /**
   * 更新訂單狀態 CreateIng -> PayIng (訂單 和 訂單中間表)
   * 更新多筆訂單 包含中間表 和訂單表 多對多
   **/
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public List<Order> updateOrderStatusToPayIng(List<Long> orderIds)
      throws NotFoundOrderException {
    //確定有更新的訂單
    List<Long> updateOrderIds = new ArrayList<>();

    //重中間表拿到所有訂單
    List<OrderStockMiddle> orderStockMiddles = orderStockMiddleDao.findOrderIds(orderIds);

    if (orderStockMiddles.size() == 0) {
      throw new NotFoundOrderException();
    }

    //修改每單筆訂單
    for (OrderStockMiddle orderStockMiddle : orderStockMiddles) {
      Order order = orderDao.findById(orderStockMiddle.getOrder_id());
      if (order == null) {
        throw new NotFoundOrderException();
      }


      //把 CreateIng 訂單改成 PayIng 中間狀態
      if (order.getStatus() == OrderStatusEnum.CreateIng.code) {
        Order build = Order.builder().build();
        build.setId(order.getId());
        build.setStatus(OrderStatusEnum.PayIng.code);
        updateOrder(build);

        updateOrderIds.add(orderStockMiddle.getOrder_id());
      }else {
        break;
      }
    }

    if(!updateOrderIds.isEmpty()){
      //更新中間表狀態
      orderStockMiddleDao.updateOrderStatusByOrderIdList(OrderStatusEnum.PayIng.code,
          updateOrderIds);

    }


    List<Order> orderList = getOrderList(updateOrderIds);
    return orderList;
  }

  //根據中間表查詢OrderList
  public List<Order> getOrderList(List<Long> middleOrderIds) {

    List<Order> list = new ArrayList<>();
    for (Long orderId : middleOrderIds) {
      Order order = getOrderById(orderId);
      list.add(order);
    }
    return list;
  }


  //取得所有訂單
  public List<Order> getAllOrderList() {

    List<Order> orderList = orderDao.findAll();

    return orderList;

  }


  //新增訂單
  public boolean addOrder(Order order) {
    boolean b = orderDao.addOrder(order);
    return b;
  }

  //更新訂單
  public int updateOrder(Order order) {
    order.setUpdate_time(new Date());
    return orderDao.updateOrder(order);
  }


  //查詢訂單 id
  public Order getOrderById(Long id) {

    Order order = orderDao.findById(id);

    if (order != null) {
      return order;
    }
    return null;
  }

}
