package test.mapper;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.example.dao.OrderStockMiddleDao;
import org.example.entities.middle.OrderStockMiddle;
import org.junit.Test;
import test.BastTest;

public class OrderStockDaoMapperTest extends BastTest {

  @Resource
  private OrderStockMiddleDao dao;

  @Test
  public void findAllTest(){
    List<OrderStockMiddle> all = dao.findAll();
    for (OrderStockMiddle orderStockMiddle : all) {
      System.out.println(orderStockMiddle);
    }
  }

  @Test
  public void findById(){
    OrderStockMiddle byId = dao.findById(1L);
    System.out.println(byId);
  }

  @Test
  public void addOrderStockMiddleTest(){
    OrderStockMiddle build = OrderStockMiddle
        .builder()
        .order_id(123L)
        .stock_id(456L)
        .deducted_quantity(324)
        .status(1)
        .update_time(new Date())
        .create_time(new Date())
        .build();
    boolean b = dao.addOrderStockMiddle(build);
    System.out.println(b);
  }

  @Test
  public void updateOrderStockMiddleTest(){
    OrderStockMiddle build = OrderStockMiddle
        .builder()
        .id(1L)
        .order_id(888L)
        .stock_id(999L)
        .deducted_quantity(12333)
        .status(0)
        .update_time(new Date())
        .create_time(new Date())
        .build();
    int i = dao.updateOrderStockMiddle(build);
    System.out.println(i);
  }






}
