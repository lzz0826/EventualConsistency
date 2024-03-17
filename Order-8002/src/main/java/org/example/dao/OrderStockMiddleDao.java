package org.example.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entities.middle.OrderStockMiddle;

@Mapper
public interface OrderStockMiddleDao {


  public boolean addOrderStockMiddle(OrderStockMiddle orderStockMiddle);

  public OrderStockMiddle findById(Long id);

  public List<OrderStockMiddle> findByIds(@Param("ids") List<Long> ids);

  public List<OrderStockMiddle> findOrderId(@Param("order_id") Long order_id);

  public List<OrderStockMiddle> findOrderIds(@Param("orderIds") List<Long> orderIds);

  public List<OrderStockMiddle> findAll();

  public int updateOrderStockMiddle(OrderStockMiddle orderStockMiddle);

  public int updateOrderStatusByOrderIdList(int status , List<Long> orderIds);



}
