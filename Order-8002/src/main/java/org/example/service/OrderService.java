package org.example.service;

import static org.example.client.service.StockClientService.RepStock;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.example.client.service.StockClientService;
import org.example.common.BaseResp;
import org.example.controller.rep.CreateOrderReq;
import org.example.dao.OrderDao;
import org.example.entities.Order;
import org.example.entities.Stock;
import org.example.exception.NoStockException;
import org.example.exception.OkHttpGetException;
import org.springframework.stereotype.Service;


@Service
public class OrderService {


  @Resource
  private StockClientService stockClientService;

  @Resource
  private OrderDao dao;

  public boolean createOrder(String product_name) throws OkHttpGetException, NoStockException {

    BaseResp<Stock> stockByProductName = stockClientService.getStockByProductName(product_name);
    Stock stock = RepStock(stockByProductName);

    if(stock == null){
      throw new NoStockException();
    }

    Order order = Order
        .builder()
        .stock_id(stock.getId())
        .create_time(new Date())
        .update_time(new Date())
        .build();

    boolean b = dao.addOrder(order);

    return b;
  }



  public List<Order> getAllOrderList(){

    List<Order> orderList = dao.findAll();

    return orderList;

  }


  public boolean addOrder(Order order){
    boolean b = dao.addOrder(order);
    return b;
  }



}
