package test;

import javax.annotation.Resource;
import org.example.exception.NoStockException;
import org.example.service.StockService;
import org.junit.Test;

public class StockServiceTest extends BastTest{

  @Resource
  private StockService stockService;

  @Test
  public void deductedStockQuantityTest() throws NoStockException {

    boolean b = stockService.deductedStockQuantity(1L,77);
    System.out.println(b);
  }

}
