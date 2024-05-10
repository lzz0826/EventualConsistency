package org.example.service;

import io.seata.core.context.RootContext;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.example.dao.StockDao;
import org.example.dao.StockOnDoLogDao;
import org.example.entities.Stock;
import org.example.entities.StockOnDoLog;
import org.example.enums.OperationTypeEnum;
import org.example.enums.StockOnDoLogEnum;
import org.example.exception.AddStockOnDoLogException;
import org.example.exception.NoStockException;
import org.example.exception.UpdateStockException;
import org.example.mq.CheckStockMq;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.example.config.RabbitMqConfig.Stock_Event_Exchange;
import static org.example.config.RabbitMqConfig.Stock_Locked_Key;

@Service
@Log4j2
public class StockService {

  @Resource
  public StockDao stockDao;

  public Stock getStockById(Long id){

      return stockDao.findById(id);
  }

  public Stock getStockByProductName(String productName){
      return stockDao.findByProductName(productName);
  }

  public List<Stock> getStockByProductNameList(List<String> productNames){
    return stockDao.findByProductNameList(productNames);
  }


  public int increaseQuantity(Long id , int quantity){
    return stockDao.increaseQuantity(id,quantity,new Date());
  }


  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ , rollbackFor = Exception.class)
  public boolean updateStock(Stock stock) throws UpdateStockException {
    stock.setUpdate_time(new Date());
    int i = stockDao.updateStock(stock);
    if(i <= 0){
      throw new UpdateStockException();
    }
    return true;
  }




}
