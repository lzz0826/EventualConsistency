package org.example.service;

import io.seata.core.context.RootContext;
import jakarta.annotation.Resource;
import java.util.Date;
import lombok.extern.log4j.Log4j2;
import org.example.dao.StockDao;
import org.example.entities.Stock;
import org.example.exception.NoStockException;
import org.example.exception.UpdateStockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
public class StockService {

  @Resource
  public StockDao dao;

  public Stock getStockById(Long id){

    Stock stock = dao.findById(id);

    return stock;
  }

  public Stock getStockByProductName(String productName){
    Stock stock = dao.findByProductName(productName);
    return stock;
  }


  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ , rollbackFor = Exception.class)
  public boolean updateStock(Stock stock) throws UpdateStockException {
    stock.setUpdate_time(new Date());
    int i = dao.updateStock(stock);
    if(i <= 0){
      throw new UpdateStockException();
    }
    return true;
  }

  /**
   * 扣庫存 檢查庫存後 在更新(在同一段 sql執行確保原子性)
   */
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ , rollbackFor = Exception.class)
  public boolean deductedStockQuantity(Long id , int quantity)
      throws NoStockException {

    //TODO Seata全局事务id=================>null 待處理
    log.info("Seata全局事务id=================>{}", RootContext.getXID());


    Stock byId = dao.findById(id);

    if(byId == null){
      throw new NoStockException();
    }

    Integer stockQuantity = byId.getQuantity();

    if(stockQuantity - quantity < 0 ){
      throw new NoStockException();
    }

    int b = dao.deductedQuantity(id,quantity,new Date());

    if(b <= 0){
      throw new NoStockException();
    }
    return true;

  }


}
