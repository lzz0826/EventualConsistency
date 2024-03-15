package test.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.example.dao.OrderDao;
import org.example.entities.Order;
import org.junit.Test;
import test.BastTest;

public class OrderDaoMapperTest extends BastTest {

  @Resource
  private OrderDao dao;

  @Test
  public void addOrderRepIdTest(){
    Order build = Order.builder()
        .stock_id(418861L)
        .price(new BigDecimal(22232.3))
        .type(1)
        .status(1)
        .update_time(new Date())
        .create_time(new Date())
        .build();

    Long b = dao.addOrderRepId(build);

    Long generatedId = build.getId();

    Integer type = build.getType();

    System.out.println(b);
    //獲取返回的id
    System.out.println(generatedId);
    System.out.println(type);

  }


  @Test
  public void testFindById(){

    Order byId = dao.findById(5L);
    System.out.println(byId);
  }

  @Test
  public void testFindAll(){

    List<Order> all = dao.findAll();
    for (Order order : all) {
      System.out.println(order);
    }
  }

  @Test
  public void testAddOrder(){
    Order build = Order.builder()
        .stock_id(42L)
        .price(new BigDecimal(22232.3))
        .type(1)
        .status(1)
        .update_time(new Date())
        .create_time(new Date())
        .build();
    boolean b = dao.addOrder(build);
    System.out.println(b);
  }

  @Test
  public void testUpdateOrder(){
    Order build = Order.builder()
        .id(1L)
        .stock_id(233L)
        .price(new BigDecimal(2332.3))
        .type(0)
        .status(0)
        .update_time(new Date())
        .create_time(new Date())
        .build();
    int i = dao.updateOrder(build);
    System.out.println(i);
  }


}
