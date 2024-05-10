package org.example.service;


import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.example.dao.OrderStockMiddleDao;
import org.example.entities.middle.OrderStockMiddle;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Log4j2
public class OrderStockMiddleService {
    @Resource
    private OrderStockMiddleDao orderStockMiddleDao;


    public int updateOrderStatusByOrderId(Long order_id , int status ){
        return orderStockMiddleDao.updateOrderStatusByOrderId(order_id,status,new Date());
    }

    public int updateOrderStockMiddle(OrderStockMiddle orderStockMiddle){
        orderStockMiddle.setCreate_time(new Date());
        return orderStockMiddleDao.updateOrderStockMiddle(orderStockMiddle);
    }








}
