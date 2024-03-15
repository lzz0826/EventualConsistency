package org.example.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.example.entities.Order;
import org.example.entities.middle.OrderStockMiddle;

@Mapper
public interface OrderStockMiddleDao {


  public boolean addOrderStockMiddle(OrderStockMiddle orderStockMiddle);

  public OrderStockMiddle findById(Long id);

  public List<OrderStockMiddle> findAll();

  public int updateOrderStockMiddle(OrderStockMiddle orderStockMiddle);


}
