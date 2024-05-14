package org.example.service;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.*;

import lombok.extern.log4j.Log4j2;
import org.example.common.StatusCode;
import org.example.dao.OrderDao;
import org.example.dao.OrderStockMiddleDao;
import org.example.entities.Order;
import org.example.entities.middle.OrderStockMiddle;
import org.example.enums.OrderStatusEnum;
import org.example.exception.AddOrderException;
import org.example.exception.NotFoundOrderException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Log4j2
@Service
public class OrderService {


  @Resource
  private OrderDao orderDao;

  @Resource
  private OrderStockMiddleService orderStockMiddleService;

  @Resource
  private OrderStockMiddleDao orderStockMiddleDao;

  /**
   * 創建訂單
   * toToPrice : 總金額
   **/
  public Order createOrder(BigDecimal toToPrice) throws AddOrderException {
    Order order = Order
            .builder()
            .price(toToPrice)
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
    return order;
  }


  /**
   * 更新訂單狀態 CreateIng -> PayIng (訂單 和 訂單中間表)
   * 更新多筆訂單 包含中間表 和訂單表 多對多
   **/
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public List<Order> updateOrderStatusToPayIng(List<Long> orderIds) throws NotFoundOrderException {
    //確定有更新的訂單
    List<Long> updateOrderIds = new ArrayList<>();

    List<OrderStockMiddle> orderStockMiddles = checkOrderStockMiddle(orderIds);

    updateOrderIds = orderStockMiddleService.updateStockMiddlesToPayIng(orderStockMiddles,updateOrderIds);

    if (updateOrderIds.isEmpty()){
      throw new NotFoundOrderException();
    }
    updateOrderStatusByIds(updateOrderIds,OrderStatusEnum.PayIng.code);
    return getOrderList(updateOrderIds);
  }

  /**
   * 更新訂單狀態 PayIng -> Success (訂單 和 訂單中間表)
   * 更新多筆訂單 包含中間表 和訂單表 多對多
   **/
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public List<Order> updateOrderStatusToSuccess(List<Long> orderIds) throws NotFoundOrderException {
    List<Long> updateOrderIds = new ArrayList<>();

    List<OrderStockMiddle> orderStockMiddles = checkOrderStockMiddle(orderIds);

    updateOrderIds = orderStockMiddleService.updateStockMiddlesToSuccess(orderStockMiddles,updateOrderIds);

    if (updateOrderIds.isEmpty()){
      throw new NotFoundOrderException();
    }
    updateOrderStatusByIds(updateOrderIds,OrderStatusEnum.Success.code);
    return getOrderList(updateOrderIds);
  }

  /**
   * 更新訂單狀態 PayIng -> Success (訂單 和 訂單中間表)
   * 更新多筆訂單 包含中間表 和訂單表 多對多
   **/
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public List<Order> updateOrderStatusToFail(List<Long> orderIds) throws NotFoundOrderException {
    List<Long> updateOrderIds = new ArrayList<>();

    List<OrderStockMiddle> orderStockMiddles = checkOrderStockMiddle(orderIds);

    updateOrderIds = orderStockMiddleService.updateStockMiddlesToFail(orderStockMiddles,updateOrderIds);

    if (updateOrderIds.isEmpty()){
      throw new NotFoundOrderException();
    }
    updateOrderStatusByIds(updateOrderIds,OrderStatusEnum.Fail.code);
    return getOrderList(updateOrderIds);
  }

  /**
   * 檢查中間表是否存在
   **/
  private List<OrderStockMiddle> checkOrderStockMiddle(List<Long> orderIds) throws NotFoundOrderException {
    //重中間表拿到所有訂單
    List<OrderStockMiddle> orderStockMiddles = orderStockMiddleDao.findOrderIds(orderIds);

    if (orderStockMiddles.isEmpty()) {
      throw new NotFoundOrderException();
    }
    return orderStockMiddles;
  }




  public List<Order> getOrderList(List<Long> orderIds) {
    return orderDao.findByIds(orderIds);
  }


  //取得所有訂單
  public List<Order> getAllOrderList() {

      return orderDao.findAll();

  }

  //新增訂單
  public boolean addOrder(Order order) {
      return orderDao.addOrder(order);
  }

  //更新訂單
  public int updateOrder(Order order) {
    order.setUpdate_time(new Date());
    return orderDao.updateOrder(order);
  }


  //查詢訂單 id
  public Order getOrderById(Long id) {
      return orderDao.findById(id);
  }

  //查詢訂單 ids
  public List<Order> getOrderByIds(List<Long> ids) {
    return orderDao.findByIds(ids);
  }

  //更新訂單狀態
  public int updateOrderStatus(Long id , int status){
    return orderDao.updateOrderStatus(id,status,new Date());

  }

  public int updateOrderStatusByIds(List<Long> ids , int status){
    return orderDao.updateOrderStatusByIds(ids,status);
  }

}
