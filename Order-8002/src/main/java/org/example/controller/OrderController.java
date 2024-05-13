package org.example.controller;


import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.example.common.BaseResp;
import org.example.controller.rep.CreateOrderMqReq;
import org.example.controller.rep.CreateOrderReq;
import org.example.entities.Order;
import org.example.exception.*;
import org.example.service.OrderSeataService;
import org.example.service.OrderService;
import org.example.service.OrderMqService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import static org.example.mq.MqStaticResource.Order_Create_Order_Key;
import static org.example.mq.MqStaticResource.Order_Event_Exchange;

@RestController
public class OrderController {

  @Resource
  private OrderService orderService;
  @Resource
  private OrderMqService orderMqService;
  @Resource
  private OrderSeataService orderSeataService;



  /**
   * 創建訂單 Seata 強一致
   *  Seata 分布式事務測試 @GlobalTransactional(目前是直連 沒有搭配服務註冊)
   *  需要在 每個要分布式事務服務 DB創建 Seata表 和resources下添加 file.conf.registry.conf (需要看使用哪個ROM)
   *  Seata1.7.1 版本問題待解決(至少要java11) 需要升級spring boot 3.0 或是 降版Seata1.5.2
   **/
  @PostMapping("/createOrderSeata")
  public BaseResp<String> createOrderSeata(@RequestBody @Valid CreateOrderReq req)
      throws OkHttpGetException, NoStockException, DeductedStockQuantityException, AddOrderException, AddOrderStockMiddleException {

    boolean order = orderSeataService.createOrderSeata(req.getProduct_name(),req.getQuantity());

    if(order){
      return BaseResp.ok("成功");
    }
    return BaseResp.ok("失敗");

  }

  /**
   * 創建訂單 Mq 最終一致
   **/
  @PostMapping("/createOrderMq")
  public BaseResp<String> createOrderMq(@RequestBody @Valid CreateOrderMqReq req)
          throws NoStockException, DeductedStockQuantityException, AddOrderException, AddOrderStockMiddleException {

    boolean order = orderMqService.createOrderMq(req.getProduct_quantity());

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

  @GetMapping("/getOrderById/{id}")
  private BaseResp<Order> getOrderById(@PathVariable("id")Long id){
    Order order = orderService.getOrderById(id);
    return BaseResp.ok(order);
  }

  @Resource
  private RabbitTemplate rabbitTemplate;

  @GetMapping("/testOrderMq")
  private BaseResp<String> testOrderMq(){

    Order build = Order.builder()
            .id(99L)
            .price(new BigDecimal(100))
            .type(1)
            .status(1)
            .create_time(new Date())
            .update_time(new Date())
            .build();
    //TODO 創建訂單 狀態 代支付

    //發送MQ延遲隊列 時間超過未支付改成 失敗
    rabbitTemplate.convertAndSend(Order_Event_Exchange,Order_Create_Order_Key,build);
    return BaseResp.ok("成功");
  }




}
