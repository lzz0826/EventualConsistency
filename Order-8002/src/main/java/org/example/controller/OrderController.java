package org.example.controller;


import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.example.client.service.StockClientService;
import org.example.common.BaseResp;
import org.example.controller.rep.CreateOrderReq;
import org.example.entities.Order;
import org.example.exception.AddOrderException;
import org.example.exception.AddOrderStockMiddleException;
import org.example.exception.DeductedStockQuantityException;
import org.example.exception.NoStockException;
import org.example.exception.OkHttpGetException;
import org.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {



  @Autowired
  private OrderService orderService;

  @Resource
  private StockClientService stockClientService;



  //Seata 分布式事務測試 @GlobalTransactional(目前是直連 沒有搭配服務註冊)
  //需要在 每個要分布式事務 創建 Seata表
  //Seata1.7.1 版本問題待解決(至少要java11) 需要升級spring boot 3.0 或是 降版Seata1.5.2
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
