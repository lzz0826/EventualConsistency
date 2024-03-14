package test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.example.dao.StockDao;
import org.example.entities.Stock;
import org.junit.Test;

public class MapperTest extends BastTest{

  @Resource
  private StockDao dao;

  @Test
  public void testFindAll(){

    List<Stock> all = dao.findAll();
    for (Stock stock : all) {
      System.out.println(stock);
    }

  }

  @Test
  public void testFindById(){
    Stock byId = dao.findById(1L);
    System.out.println(byId);

  }

  @Test
  public void testAddStock(){

    Stock build = Stock.builder()
        .product_id(1L)
        .product_name("大大")
        .price(new BigDecimal(234.12))
        .type(1)
        .status(1)
        .update_time(new Date())
        .create_time(new Date())
        .build();
    boolean b = dao.addStock(build);
    System.out.println(b);

  }

  @Test
  public void testUpdateStock(){

    Stock build = Stock.builder()
        .id(1L)
        .product_id(13L)
        .product_name("小小")
        .price(new BigDecimal(23444.12))
        .type(0)
        .status(0)
        .update_time(new Date())
        .create_time(new Date())
        .build();
    int i = dao.updateStock(build);
    System.out.println(i);

  }




}
