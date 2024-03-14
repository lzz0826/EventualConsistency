package org.example.service;

import java.util.Date;
import javax.annotation.Resource;
import org.example.dao.StockDao;
import org.example.entities.Stock;
import org.example.exception.UpdateStockException;
import org.springframework.stereotype.Service;

@Service
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

  public boolean updateStock(Stock stock) throws UpdateStockException {
    stock.setUpdate_time(new Date());
    int i = dao.updateStock(stock);
    if(i <= 0){
      throw new UpdateStockException();
    }
    return true;

  }


}
