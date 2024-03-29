package org.example.controller;


import javax.annotation.Resource;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
import org.example.controller.req.DeductedStockQuantityReq;
import org.example.controller.req.UpdateStockReq;
import org.example.entities.Stock;
import org.example.exception.NoStockException;
import org.example.exception.UpdateStockException;
import org.example.service.StockService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {


  @Resource
  public StockService stockService;

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
  @PostMapping("/deductedStockQuantity")
  public BaseResp<String> deductedStockQuantity(@RequestBody @Valid DeductedStockQuantityReq req)
      throws UpdateStockException, NoStockException {

    boolean b = stockService.deductedStockQuantity(req.getId(), req.getQuantity());

    return BaseResp.ok(String.valueOf(b),StatusCode.Success);
  }






}
