package org.example.dao;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entities.Stock;

@Mapper
public interface StockDao {

  public boolean addStock(Stock stock);

  public Stock findById(Long id);

  public List<Stock> findAll();

  public int updateStock(Stock stock);


  public Stock findByProductName(String ProductName);

  public List<Stock>findByProductNameList(List<String> product_names);


  public int deductedQuantity(@Param("id") Long id ,@Param("deducted_quantity") int deductedQuantity,
      @Param("update_time") Date date);

  public int increaseQuantity(@Param("id") Long id ,@Param("increase_quantity") int increaseQuantity,
                              @Param("update_time") Date date);


}
