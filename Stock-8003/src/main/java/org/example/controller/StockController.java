package org.example.controller;


import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
import org.example.controller.req.DeductedStockQuantityMqReq;
import org.example.controller.req.DeductedStockQuantityReq;
import org.example.controller.req.UpdateStockReq;
import org.example.entities.Stock;
import org.example.exception.AddStockOnDoLogException;
import org.example.exception.NoStockException;
import org.example.exception.UpdateStockException;
import org.example.mq.CheckStockMq;
import org.example.service.StockMqService;
import org.example.service.StockSeataService;
import org.example.service.StockService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.example.config.RabbitMqConfig.*;

@RestController
public class StockController {


  @Resource
  private StockService stockService;

  @Resource
  private StockMqService stockMqService;

  @Resource
  private StockSeataService stockSeataService;

  @GetMapping("/getStockById/{id}")
  public BaseResp<Stock> findStockById(@PathVariable("id")Long id){
    Stock stock = stockService.getStockById(id);
    return BaseResp.ok(stock);
  }


  @GetMapping("/getStockByProductName/{productName}")
  public BaseResp<Stock> getStockByProductName(@PathVariable("productName")String productName){
    Stock stock = stockService.getStockByProductName(productName);
    return BaseResp.ok(stock);
  }

  @PostMapping("/getStockByProductNames")
  public BaseResp<List<Stock>> getStockByProductNames(@RequestBody()List<String> productNames){
    List<Stock> stock = stockService.getStockByProductNameList(productNames);
    return BaseResp.ok(stock);
  }


  @PostMapping("/updateStock")
  public BaseResp<String> updateStock(@RequestBody @Valid UpdateStockReq req) throws UpdateStockException {

    Stock stock = Stock.builder().build();

    BeanUtils.copyProperties(req,stock);

    boolean b = stockService.updateStock(stock);

    return BaseResp.ok(StatusCode.Success);
  }


  /**
   * 扣庫存 檢查庫存後 在更新(在同一段 sql執行確保原子性)
   */
  //Seata 分布式事務測試 @GlobalTransactional
  @PostMapping("/deductedStockQuantity")
  public BaseResp<String> deductedStockQuantity(@RequestBody @Valid DeductedStockQuantityReq req)
      throws NoStockException {

    boolean b = stockSeataService.deductedStockQuantity(req.getId(), req.getQuantity());

    return BaseResp.ok(String.valueOf(b),StatusCode.Success);
  }

  /**
   * 扣庫存 使用MQ做最終一致性
   */
  @PostMapping("/deductedStockQuantityMq")
  public BaseResp<String> deductedStockQuantityMq(@RequestBody @Valid DeductedStockQuantityMqReq req)
          throws NoStockException, AddStockOnDoLogException {
    boolean b = stockMqService.deductedStockQuantityMq(req.getStockId(), req.getOrderId(),req.getQuantity());
    //TODO 失敗返回 deductedStockQuantityMq 失敗本地會回滾 通知order服務 讓order也本地回滾
    return BaseResp.ok(String.valueOf(b),StatusCode.Success);
  }



  @Resource
  private RabbitTemplate rabbitTemplate;

  @GetMapping("/testOrderMq")
  private BaseResp<String> testOrderMq(){
    CheckStockMq build = CheckStockMq
            .builder()
            .stock_undo_log_id(1L)
            .stock_id(2L)
            .order_id(3L)
            .build();
    //使用交換機+路由
    rabbitTemplate.convertAndSend(Stock_Event_Exchange,Stock_Locked_Key,build);
    return BaseResp.ok("成功");
  }






}
