package org.example.service;

import jakarta.annotation.Resource;
import java.util.*;

import lombok.extern.log4j.Log4j2;
import org.example.dao.OrderDao;
import org.example.dao.OrderStockMiddleDao;
import org.example.entities.Order;
import org.example.entities.middle.OrderStockMiddle;
import org.example.enums.OrderStatusEnum;
import org.example.enums.OrderStockMiddleStatusEnum;
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
  private OrderStockMiddleDao orderStockMiddleDao;


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

    if (orderStockMiddles.isEmpty()) {
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
      orderStockMiddleDao.updateOrderStatusByOrderIdList(updateOrderIds, OrderStockMiddleStatusEnum.PayIng.code);

    }

      return getOrderList(updateOrderIds);
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

  //更新訂單狀態
  public int updateOrderStatus(Long id , int status){
    return orderDao.updateOrderStatus(id,status,new Date());

  }

}
