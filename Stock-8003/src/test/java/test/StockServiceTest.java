package test;

import jakarta.annotation.Resource;
import org.example.exception.NoStockException;
import org.example.service.StockSeataService;
import org.example.service.StockService;
import org.junit.Test;

public class StockServiceTest extends BastTest{

  @Resource
  private StockService stockService;

  @Resource
  private StockSeataService stockSeataService;

  @Test
  public void deductedStockQuantityTest() throws NoStockException {

    boolean b = stockSeataService.deductedStockQuantity(1L,77);
    System.out.println(b);
  }

}
