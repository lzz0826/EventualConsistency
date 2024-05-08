package org.example.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.entities.Stock;
import org.example.entities.StockOnDoLog;


@Mapper
public interface StockOnDoLogDao {
  public boolean addStockOnDoLog(StockOnDoLog stockOnDoLog);

  public StockOnDoLog findById(Long id);

  public StockOnDoLog findByStockIdAndOrderId(Long stock_id ,Long order_id);

  public int updateStockOnDoLog(StockOnDoLog stockOnDoLog);




}
