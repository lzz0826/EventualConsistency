package test.mapper;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.example.dao.OrderStockMiddleDao;
import org.example.entities.middle.OrderStockMiddle;
import org.example.enums.OrderStockMiddleStatusEnum;
import org.junit.Test;
import test.BastTest;

public class OrderStockDaoMapperTest extends BastTest {

  @Resource
  private OrderStockMiddleDao dao;

  @Test
  public void findOrderIdsTest(){

    List<Long> list = new ArrayList<>();
    list.add(20L);
    list.add(21L);
    list.add(22L);
    List<OrderStockMiddle> orderIds = dao.findOrderIds(list);
    for (OrderStockMiddle orderId : orderIds) {
      System.out.println(orderId);
    }

  }

  @Test
  public void updateOrderStatusByIdListTest(){
    List<Long> list = new ArrayList<>();
    list.add(100L);
    list.add(15L);
    int i = dao.updateOrderStatusByOrderIdList(list, OrderStockMiddleStatusEnum.PayIng.code);
    System.out.println(i);
  }

  @Test
  public void testUpdateOrderStatusByOrderId(){
    int i = dao.updateOrderStatusByOrderId(100L , 1,new Date());
    System.out.println(i);
  }


  @Test
  public void findOrderIdTest(){

    List<OrderStockMiddle> orderId = dao.findOrderId(24L);
    for (OrderStockMiddle orderStockMiddle : orderId) {
      System.out.println(orderStockMiddle);
    }
  }


  @Test
  public void findByIdsTest(){

    List<Long> lists = new ArrayList<>();
    lists.add(1L);
    lists.add(2L);
    lists.add(3L);
    lists.add(4L);

    List<OrderStockMiddle> byIds = dao.findByIds(lists);
    for (OrderStockMiddle byId : byIds) {
      System.out.println(byId);
    }
  }

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
