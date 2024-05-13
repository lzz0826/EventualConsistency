package test.service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import org.example.entities.Order;
import org.example.exception.AddOrderException;
import org.example.exception.AddOrderStockMiddleException;
import org.example.exception.DeductedStockQuantityException;
import org.example.exception.NoStockException;
import org.example.exception.NotFoundOrderException;
import org.example.exception.OkHttpGetException;
import org.example.service.OrderSeataService;
import org.example.service.OrderService;
import org.junit.Test;
import test.BastTest;

public class OrderServiceTest extends BastTest {

  @Resource
  private OrderService orderService;

  @Resource
  private OrderSeataService orderSeataService;


  @Test
  public void updateOrderStatusToPayIngTest() throws NotFoundOrderException {

    List<Long> ids = new ArrayList<>();
    ids.add(112L);
    ids.add(111L);


    List<Order> list = orderService.updateOrderStatusToPayIng(ids);

    System.out.println("最後返回:");
    for (Order order : list) {
      System.out.println(order);
    }

  }


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
        boolean order = orderSeataService.createOrderSeata(product_name,quantity);
        System.out.println(order);

      }catch (Exception e){
        e.printStackTrace();
      }
    }




  }

}
