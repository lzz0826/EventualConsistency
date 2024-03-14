package org.example.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.example.entities.Order;

@Mapper
public interface OrderDao {

  public boolean addOrder(Order order);

  public Order findById(Long id);

  public List<Order> findAll();

  public int updateOrder(Order order);




}
