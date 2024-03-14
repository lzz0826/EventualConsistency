package org.example.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.example.entities.Stock;

@Mapper
public interface StockDao {

  public boolean addStock(Stock stock);

  public Stock findById(Long id);

  public List<Stock> findAll();

  public int updateStock(Stock stock);




}
