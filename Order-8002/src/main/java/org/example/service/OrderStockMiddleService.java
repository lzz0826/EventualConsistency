package org.example.service;


import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.example.common.StatusCode;
import org.example.dao.OrderDao;
import org.example.dao.OrderStockMiddleDao;
import org.example.entities.Order;
import org.example.entities.Stock;
import org.example.entities.middle.OrderStockMiddle;
import org.example.enums.OrderStatusEnum;
import org.example.enums.OrderStockMiddleStatusEnum;
import org.example.exception.AddOrderStockMiddleException;
import org.example.exception.NotFoundOrderException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Log4j2
public class OrderStockMiddleService {
    @Resource
    private OrderStockMiddleDao orderStockMiddleDao;

    @Resource
    private OrderDao orderDao;



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

    /**
     * 更新中間表 返回確定有更新的訂單
     * 更新訂單狀態 CreateIng -> PayIng
     **/
    public List<Long> updateStockMiddlesToPayIng(List<OrderStockMiddle> orderStockMiddles , List<Long> updateOrderIds) throws NotFoundOrderException {

        checkStatusEnum(orderStockMiddles,updateOrderIds,OrderStatusEnum.CreateIng);
        if(!updateOrderIds.isEmpty()){
            //更新中間表 sCreateIng 訂單改成 PayIng 中間狀態
            orderStockMiddleDao.updateOrderStatusByOrderIdList(updateOrderIds, OrderStockMiddleStatusEnum.PayIng.code);
        }
        return updateOrderIds;
    }

    private void checkStatusEnum(List<OrderStockMiddle> orderStockMiddles,List<Long> updateOrderIds,OrderStatusEnum statusEnum) throws NotFoundOrderException {
        for (OrderStockMiddle orderStockMiddle : orderStockMiddles) {
            Order order = orderDao.findById(orderStockMiddle.getOrder_id());
            if (order == null) {
                throw new NotFoundOrderException();
            }
            if (order.getStatus() == statusEnum.code) {
                updateOrderIds.add(orderStockMiddle.getOrder_id());
            }
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
