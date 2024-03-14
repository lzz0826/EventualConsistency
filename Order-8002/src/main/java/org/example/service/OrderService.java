package org.example.service;

import java.util.List;
import javax.annotation.Resource;
import org.example.dao.OrderDao;
import org.example.entities.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderService {


  @Resource
  private OrderDao dao;

  public List<Order> getAllOrderList(){

    List<Order> orderList = dao.findAll();

    return orderList;

  }


  public boolean addOrder(Order order){
    boolean b = dao.addOrder(order);
    return b;
  }



}
