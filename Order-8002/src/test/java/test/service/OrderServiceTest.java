package test.service;

import javax.annotation.Resource;
import org.example.exception.AddOrderException;
import org.example.exception.AddOrderStockMiddleException;
import org.example.exception.DeductedStockQuantityException;
import org.example.exception.NoStockException;
import org.example.exception.OkHttpGetException;
import org.example.service.OrderService;
import org.junit.Test;
import test.BastTest;

public class OrderServiceTest extends BastTest {

  @Resource
  private OrderService orderService;


  //測試FOR回圈裡 回滾範圍 (只會回滾單次異常循環)
  @Test
  //測試FOR回圈裡 回滾範圍 要整個for回滾外層也要加上@Transactional
//  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ , rollbackFor = Exception.class)
  public void createOrderTransactionalTest()
      throws NoStockException, AddOrderException, AddOrderStockMiddleException, DeductedStockQuantityException, OkHttpGetException {
    String product_name = "大大";
    int quantity = 1;

    for(int i = 0 ; i < 5 ; i++){

      try{
        if(i == 3){
          int ee = 10/0;
        }
        boolean order = orderService.createOrder(product_name,quantity);
        System.out.println(order);

      }catch (Exception e){
        e.printStackTrace();
      }
    }




  }

}
