package org.example.service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import lombok.extern.log4j.Log4j2;
import org.example.client.service.StockClientService;
import org.example.entities.Order;
import org.example.entities.Stock;
import org.example.entities.middle.OrderStockMiddle;
import org.example.exception.*;
import org.example.mq.CheckOrderMq;
import org.example.mq.OrderRollbackNotifyMq;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.example.mq.MqStaticResource.*;

@Log4j2
@Service
public class OrderMqService {

    @Resource
    private StockClientService stockClientService;

    @Resource
    private OrderService orderService;

    @Resource
    private OrderStockMiddleService orderStockMiddleService;

    @Resource
    private RabbitTemplate rabbitTemplate;



    /**
     * 創建訂單 Mq 最終一致
     * 一個訂單 多個庫存(產品)
     * TODO product_names 改成 stockId
     **/
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public Long createOrderMq(Map<String,Integer> product_quantity)
            throws NoStockException, AddOrderException, AddOrderStockMiddleException, DeductedStockQuantityException, NotFoundOrderException {

        List<String> product_names = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : product_quantity.entrySet()) {
            product_names.add(entry.getKey());
        }
        //檢查庫存 打Stock服務
        List<Stock> stocks = stockClientService.getStockByProductNames(product_names);

        if (stocks == null) {
            throw new StockServerErrorException();
        }

        if (product_names.size() != stocks.size()){
            throw new NoStockException();
        }

        BigDecimal toToPrice = new BigDecimal(0);

        for (Stock stock : stocks) {
            //檢查查出的 stock 是否有對應
            String productName = stock.getProduct_name();
            if (!product_quantity.containsKey(productName)){
                throw new NoStockException();
            }
            //檢查庫存是否足夠
            Integer stockQuantity = stock.getQuantity();
            Integer orderQuantity = product_quantity.get(productName);
            if (stockQuantity < orderQuantity){
                throw new NoStockException();
            }
            //計算整筆訂單金額
            toToPrice = toToPrice.add(stock.getPrice().multiply(BigDecimal.valueOf(orderQuantity)));
        }
        //產生訂單
        Order order = orderService.createOrder(toToPrice);

        CheckOrderMq checkOrderMq = CheckOrderMq
                .builder()
                .order_id(order.getId())
                .build();

        for (Stock stock : stocks) {
            deductedStockQuantity(stock.getId(),order.getId(),product_quantity.get(stock.getProduct_name()));
            orderStockMiddleService.createOrderStockMiddle(order, stock,product_quantity.get(stock.getProduct_name()));
        }

        //訂單 創建中 -> 支付中
        orderService.updateOrderStatusToPayIng(new ArrayList<>(Collections.singletonList(order.getId())));

        //通知MQ
        rabbitTemplate.convertAndSend(Order_Event_Exchange,Order_Create_Order_Key,checkOrderMq);

        return order.getId();
    }

    /**
     * stockId
     * orderId
     * deductStockQuantity : 每個庫存需要扣除的數量
     **/
    public void deductedStockQuantity(Long stockId,Long orderId,int deductStockQuantity ) throws NoStockException, DeductedStockQuantityException {
        boolean deductedStockQuantity = stockClientService.deductedStockQuantityMq(String.valueOf(stockId),
                String.valueOf(orderId), String.valueOf(deductStockQuantity));
        if (!deductedStockQuantity) {
            throw new DeductedStockQuantityException();
        }
        //*如果沒有分布式事務 這邊報異常 Order會回滾(沒有天加訂單)
//    int sdf = 10/0;
    }


    /**
     * 更新訂單狀態 PayIng -> Success (訂單 和 訂單中間表)
     * 更新多筆訂單 包含中間表 和訂單表 多對多
     * key : Order_Finish_Key
     * TODO 超時後就無法再修改訂單狀態 order服務停時 訂單成功API需要處理 需要判斷狀態超時後再做其他處理
     **/
    public boolean updateOrderStatusToSuccess(Long orderId) throws NotFoundOrderException, NotFoundUpdateOrderException {
        List<Order> orders = orderService.updateOrderStatusToSuccess(new ArrayList<>(Collections.singletonList(orderId)));
        if (!orders.isEmpty()){
            List<OrderStockMiddle> middles = orderStockMiddleService.findOrderId(orderId);
            for (OrderStockMiddle middle : middles) {
                OrderRollbackNotifyMq notifyMq = OrderRollbackNotifyMq
                        .builder()
                        .order_id(orderId)
                        .stock_id(middle.getStock_id())
                        .build();
                //通知MQ
                rabbitTemplate.convertAndSend(Order_Event_Exchange,Order_Finish_Key,notifyMq);            }
            return true;
        }
        return false;
    }

    /**
     * 更新訂單狀態 PayIng -> Fail (訂單 和 訂單中間表)
     * 更新多筆訂單 包含中間表 和訂單表 多對多
     * key : Order_Release_Other_Key
     **/
    public boolean updateOrderStatusToFail(Long orderId) throws NotFoundOrderException, NotFoundUpdateOrderException {
        List<Order> orders = orderService.updateOrderStatusToFail(new ArrayList<>(Collections.singletonList(orderId)));
        if (!orders.isEmpty()){
            List<OrderStockMiddle> middles = orderStockMiddleService.findOrderId(orderId);
            for (OrderStockMiddle middle : middles) {
                OrderRollbackNotifyMq notifyMq = OrderRollbackNotifyMq
                        .builder()
                        .order_id(orderId)
                        .stock_id(middle.getStock_id())
                        .build();
                //通知MQ
                rabbitTemplate.convertAndSend(Order_Event_Exchange,Order_Release_Other_Key,notifyMq);
            }
            return true;
        }
        return false;

    }





}
