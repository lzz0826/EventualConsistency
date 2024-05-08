package org.example.service;

import io.seata.core.context.RootContext;
import jakarta.annotation.Resource;
import java.util.Date;
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

  @Resource
  private RabbitTemplate rabbitTemplate;

  @Resource
  public StockOnDoLogDao stockOnDoLogDao;

  public Stock getStockById(Long id){

      return stockDao.findById(id);
  }

  public Stock getStockByProductName(String productName){
      return stockDao.findByProductName(productName);
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

  /**
   * Seata 扣庫存 檢查庫存後 在更新(在同一段 sql執行確保原子性)
   */
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ , rollbackFor = Exception.class)
  public boolean deductedStockQuantity(Long id , int quantity)
      throws NoStockException {

    log.info("Seata全局事务id=================>{}", RootContext.getXID());


    Stock byId = stockDao.findById(id);

    if(byId == null){
      throw new NoStockException();
    }

    Integer stockQuantity = byId.getQuantity();

    if(stockQuantity - quantity < 0 ){
      throw new NoStockException();
    }

    int b = stockDao.deductedQuantity(id,quantity,new Date());

    if(b <= 0){
      throw new NoStockException();
    }
    return true;

  }

  /**
   * RabbitMq 扣庫存 最終一致性
   */
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ , rollbackFor = Exception.class)
  public boolean deductedStockQuantityMq(Long stockId ,Long orderId , int quantity)
          throws NoStockException, AddStockOnDoLogException {

    Stock byId = stockDao.findById(stockId);

    if(byId == null){
      throw new NoStockException();
    }

    Integer stockQuantity = byId.getQuantity();

    if(stockQuantity - quantity < 0 ){
      throw new NoStockException();
    }

    //扣庫存
    int b = stockDao.deductedQuantity(stockId,quantity,new Date());

    if(b <= 0){
      throw new NoStockException();
    }

    StockOnDoLog stockOnDoLog = StockOnDoLog
            .builder()
            .stock_id(stockId)
            .order_id(orderId)
            .operation_type(OperationTypeEnum.Decrease.name)
            .quantity(quantity)
            .operation_time(new Date())
            .description("扣庫存")
            .status(StockOnDoLogEnum.Wait.code)
            .update_time(new Date())
            .create_time(new Date())
            .build();
    //紀錄庫存操作
    boolean b1 = stockOnDoLogDao.addStockOnDoLog(stockOnDoLog);
    if(!b1){
      throw new AddStockOnDoLogException();
    }
    //通知MQ 之後要檢查 訂單打完此扣庫存後是否有回滾 訂單超時為支付也需要回滾
    CheckStockMq checkStockMq = CheckStockMq
            .builder()
            .stock_undo_log_id(stockOnDoLog.getId())
            .stock_id(stockId)
            .order_id(orderId)
            .build();
    rabbitTemplate.convertAndSend(Stock_Event_Exchange,Stock_Locked_Key,checkStockMq);
    return true;

  }


}
