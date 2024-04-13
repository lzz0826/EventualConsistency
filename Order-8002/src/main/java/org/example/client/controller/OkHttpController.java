package org.example.client.controller;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.example.client.service.StockClientService;
import org.example.common.BaseResp;
import org.example.entities.Stock;
import org.example.exception.OkHttpGetException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/client")
public class OkHttpController {

  @Resource
  private StockClientService stockClientService;


  @GetMapping("/findStockById/{id}")
  public BaseResp<Stock> sendMsg(@PathVariable("id")Long id) throws OkHttpGetException {
    BaseResp<Stock> baseResp = stockClientService.getStockById(id);
    return baseResp;
  }

//  @GetMapping("/test/form")
//  public String sendForm() {
//    String url = "http://localhost:8081/order/order/test/form";
//    Map<String, String> params = new HashMap<>();
//    logger.debug("message");
//    logger.info("message");
//    logger.error("message");
//    logger.trace("message");
//    logger.warn("message");
//    LogUtils.data(LogType.Error,"testTag",params);
//    params.put("id", "3442");
//    params.put("userId", "1");
//    params.put("price", "123");
//    return OkHttpUtil.post(url, params);
//  }
//
//
//
//  @GetMapping("/test/order")
//  public String sendOder() {
//    String url = "http://localhost:8081/order/order/test/order";
//    Order order = Order.builder().
//        id("999")
//        .userId("888")
//        .price(543)
//        .build();
//
//    return OkHttpUtil.post(url, null, JSON.toJSONString(order));
//  }
//
//
//  //带有嵌套泛型的对象
//  @GetMapping("/test/result")
//  public Results sendResult() {
//    String url = "http://localhost:8081/order/order/test/result";
//    Order order = Order.builder().
//        id("999")
//        .userId("888")
//        .price(543)
//        .build();
//    Results<Order> results = OkHttpUtil.post(url, null, JSON.toJSONString(order), new TypeToken<Results<Order>>() {
//    }.getType());
//    log.info(JSON.toJSONString(results));
//    return results ;
//  }
//
//
//  @GetMapping("/test/img")
//  public String sendImg() {
//    System.out.println("sendImg啟動");
//    String url = "http://localhost:8081/order/order/test/img";
//    return OkHttpUtil.postImg(url, null, "{}");
//  }
//
//
//  //异步调用
//  @GetMapping("/test/sync/img")
//  public void sendSyncImg() {
//    String url = "http://localhost:8081/order/order/test/img";
//    OkHttpUtil.downloadImg(url,  "{}");
//    log.info("请求结束");
//  }
//
//
//  public String test99() {
//    String url = "http://34.126.176.39:8080/user/user/login";
//    Map<String, String> params = new HashMap<>();
//    params.put("username", "tony");
//    params.put("password", "12345678");
//    return OkHttpUtil.post(url, params);
//  }


}