package org.example.controller;


import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.example.client.service.StockClientService;
import org.example.common.BaseResp;
import org.example.controller.rep.CreateOrderReq;
import org.example.entities.Order;
import org.example.entities.Stock;
import org.example.exception.AddOrderException;
import org.example.exception.AddOrderStockMiddleException;
import org.example.exception.DeductedStockQuantityException;
import org.example.exception.NoStockException;
import org.example.exception.OkHttpGetException;
import org.example.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

  @Resource
  private OrderService orderService;

  @Resource
  private StockClientService stockClientService;

  @PostMapping("/createOrder")
  public BaseResp<String> createOrder(@RequestBody @Valid CreateOrderReq req)
      throws OkHttpGetException, NoStockException, DeductedStockQuantityException, AddOrderException, AddOrderStockMiddleException {

    boolean order = orderService.createOrder(req.getProduct_name(),req.getQuantity());

    if(order){
      return BaseResp.ok("成功");
    }
    return BaseResp.ok("失敗");

  }

  @GetMapping("/getAllOrderList")
  public BaseResp<List<Order>> getAllOrderList(){

    List<Order> allOrderList = orderService.getAllOrderList();

    return BaseResp.ok(allOrderList);
  }




}
