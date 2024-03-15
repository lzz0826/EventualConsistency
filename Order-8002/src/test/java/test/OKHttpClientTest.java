package test;

import javax.annotation.Resource;
import org.example.client.service.StockClientService;
import org.example.common.BaseResp;
import org.example.entities.Stock;
import org.example.exception.NoStockException;
import org.example.exception.OkHttpGetException;
import org.junit.Test;

public class OKHttpClientTest extends BastTest{


  @Resource
  private StockClientService stockClientService;

  @Test
  public void deductedStockQuantityTest() throws OkHttpGetException, NoStockException {
    boolean b = stockClientService.deductedStockQuantity("1","1");
    System.out.println(b);

  }

  @Test
  public void testGetStockByProductName() throws OkHttpGetException {

    String name = "大大";

    BaseResp<Stock> stockByProductName = stockClientService.getStockByProductName(name);

    if(stockByProductName.getData() == null){
      System.out.println("沒東西");
    }else {
      System.out.println(stockByProductName.getData());
    }


  }

}
