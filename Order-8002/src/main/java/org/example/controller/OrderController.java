package org.example.controller;


import java.util.List;
import javax.annotation.Resource;
import org.example.common.BaseResp;
import org.example.entities.Order;
import org.example.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

  @Resource
  OrderService service;


  @GetMapping("getAllOrderList")
  public BaseResp<List<Order>> getAllOrderList(){

    List<Order> allOrderList = service.getAllOrderList();

    return BaseResp.ok(allOrderList);
  }




}
