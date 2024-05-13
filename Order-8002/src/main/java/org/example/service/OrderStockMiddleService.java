package org.example.service;


import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.example.common.StatusCode;
import org.example.dao.OrderStockMiddleDao;
import org.example.entities.Order;
import org.example.entities.Stock;
import org.example.entities.middle.OrderStockMiddle;
import org.example.exception.AddOrderStockMiddleException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Log4j2
public class OrderStockMiddleService {
    @Resource
    private OrderStockMiddleDao orderStockMiddleDao;


    /**
     * order
     * stock
     * deductStockQuantity : 每個庫存需要扣除的數量
     **/
    public void createOrderStockMiddle(Order order, Stock stock, int deductStockQuantity) throws AddOrderStockMiddleException {
        OrderStockMiddle orderStockMiddle = OrderStockMiddle
                .builder()
                .order_id(order.getId())
                .status(order.getStatus())
                .deducted_quantity(deductStockQuantity)
                .stock_id(stock.getId())
                .create_time(new Date())
                .update_time(new Date())
                .build();
        boolean addOrderStockMiddle = orderStockMiddleDao.addOrderStockMiddle(orderStockMiddle);
        if (!addOrderStockMiddle) {
            log.error(StatusCode.AddOrderStockMiddleFail.msg);
            throw new AddOrderStockMiddleException();
        }
    }


    public int updateOrderStatusByOrderId(Long order_id , int status ){
        return orderStockMiddleDao.updateOrderStatusByOrderId(order_id,status,new Date());
    }

    public int updateOrderStockMiddle(OrderStockMiddle orderStockMiddle){
        orderStockMiddle.setCreate_time(new Date());
        return orderStockMiddleDao.updateOrderStockMiddle(orderStockMiddle);
    }

    public List<OrderStockMiddle> findOrderId(Long orderId){
        return orderStockMiddleDao.findOrderId(orderId);
    }








}
