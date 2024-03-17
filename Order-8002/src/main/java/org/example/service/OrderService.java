package org.example.service;

import static org.example.client.service.StockClientService.RepStock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.example.client.service.StockClientService;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
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


@Service
@Log4j2
public class OrderService {


  @Resource
  private StockClientService stockClientService;

  @Resource
  private OrderDao orderDao;

  @Resource
  private OrderStockMiddleDao orderStockMiddleDao;

  /**
   * 創建訂單
   **/
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public boolean createOrder(String product_name, int quantity)
      throws OkHttpGetException, NoStockException, DeductedStockQuantityException, AddOrderException, AddOrderStockMiddleException {

    BaseResp<Stock> stockByProductName = stockClientService.getStockByProductName(product_name);
    Stock stock = RepStock(stockByProductName);

    if (stock == null) {
      throw new NoStockException();
    }

    Long id = stock.getId();

    //請求Stock服務 扣庫存 TODO 回滾只會在Order上 Stock 需要處理
    boolean deductedStockQuantity = stockClientService.deductedStockQuantity(String.valueOf(id),
        String.valueOf(quantity));

    if (!deductedStockQuantity) {

      throw new DeductedStockQuantityException();
    }

    Order order = Order
        .builder()
        .price(stock.getPrice().multiply(BigDecimal.valueOf(quantity)))
        .type(1)
        .status(OrderStatusEnum.CreateIng.code)
        .create_time(new Date())
        .update_time(new Date())
        .build();

    //TODO 這邊報異常 Order會回滾 但是Stock服務 不會
//    int sdf = 10/0;

    Long addOrder = orderDao.addOrderRepId(order);
    if (addOrder == 0) {
      log.error(StatusCode.AddOrderFail.msg);
      throw new AddOrderException();
    }

    OrderStockMiddle orderStockMiddle = OrderStockMiddle
        .builder()
        .order_id(order.getId())
        .status(order.getStatus())
        .deducted_quantity(quantity)
        .stock_id(stock.getId())
        .create_time(new Date())
        .update_time(new Date())
        .build();

    boolean addOrderStockMiddle = orderStockMiddleDao.addOrderStockMiddle(orderStockMiddle);

    if (!addOrderStockMiddle) {
      log.error(StatusCode.AddOrderStockMiddleFail.msg);
      throw new AddOrderStockMiddleException();
    }
    return true;
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
