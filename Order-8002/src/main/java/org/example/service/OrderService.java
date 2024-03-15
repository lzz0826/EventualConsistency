package org.example.service;

import static org.example.client.service.StockClientService.RepStock;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.example.client.service.StockClientService;
import org.example.common.BaseResp;
import org.example.dao.OrderDao;
import org.example.dao.OrderStockMiddleDao;
import org.example.entities.Order;
import org.example.entities.Stock;
import org.example.entities.middle.OrderStockMiddle;
import org.example.exception.DeductedStockQuantityException;
import org.example.exception.NoStockException;
import org.example.exception.OkHttpGetException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class OrderService {


  @Resource
  private StockClientService stockClientService;

  @Resource
  private OrderDao orderDao;

  @Resource
  private OrderStockMiddleDao orderStockMiddleDao;


  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ , rollbackFor = Exception.class)
  public boolean createOrder(String product_name , int quantity)
      throws OkHttpGetException, NoStockException, DeductedStockQuantityException {

    BaseResp<Stock> stockByProductName = stockClientService.getStockByProductName(product_name);
    Stock stock = RepStock(stockByProductName);

    if(stock == null){
      throw new NoStockException();
    }

    Long id = stock.getId();

    boolean deductedStockQuantity = stockClientService.deductedStockQuantity(String.valueOf(id), String.valueOf(quantity));

    if(!deductedStockQuantity){
      throw new DeductedStockQuantityException();
    }


    Order order = Order
        .builder()
        .stock_id(stock.getId())
        .create_time(new Date())
        .update_time(new Date())
        .build();

    //TODO

    OrderStockMiddle orderStockMiddle = OrderStockMiddle
        .builder()
//        .order_id()
//        .status()
//        .deducted_quantity()
//        .stock_id()
//        .create_time(new Date())
//        .update_time(new Date())
        .build();

    boolean addOrder = orderDao.addOrder(order);
    boolean addOrderStockMiddle = orderStockMiddleDao.addOrderStockMiddle(orderStockMiddle);

    return false;
  }



  public List<Order> getAllOrderList(){

    List<Order> orderList = orderDao.findAll();

    return orderList;

  }


  public boolean addOrder(Order order){
    boolean b = orderDao.addOrder(order);
    return b;
  }



}
