package org.example.service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import lombok.extern.log4j.Log4j2;
import org.example.client.service.StockClientService;
import org.example.common.StatusCode;
import org.example.dao.OrderDao;
import org.example.dao.OrderStockMiddleDao;
import org.example.entities.Order;
import org.example.entities.Stock;
import org.example.entities.middle.OrderStockMiddle;
import org.example.enums.OrderStatusEnum;
import org.example.exception.*;
import org.example.mq.CheckOrderMq;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.example.config.RabbitMqConfig.*;
import static org.example.mq.MqStaticResource.Order_Create_Order_Key;
import static org.example.mq.MqStaticResource.Order_Event_Exchange;

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
     **/
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public boolean createOrderMq(Map<String,Integer> product_quantity)
            throws NoStockException, AddOrderException, AddOrderStockMiddleException, DeductedStockQuantityException {

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

        rabbitTemplate.convertAndSend(Order_Event_Exchange,Order_Create_Order_Key,checkOrderMq);

        return true;
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




}
