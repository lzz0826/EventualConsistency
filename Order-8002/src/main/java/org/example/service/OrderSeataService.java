package org.example.service;


import static org.example.client.service.StockClientService.RepStock;

import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import lombok.extern.log4j.Log4j2;
import org.example.client.service.StockClientService;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
import org.example.dao.OrderDao;
import org.example.dao.OrderStockMiddleDao;
import org.example.entities.Order;
import org.example.entities.Stock;
import org.example.entities.middle.OrderStockMiddle;
import org.example.enums.OrderStatusEnum;
import org.example.exception.AddOrderException;
import org.example.exception.AddOrderStockMiddleException;
import org.example.exception.DeductedStockQuantityException;
import org.example.exception.NoStockException;
import org.example.exception.OkHttpGetException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Log4j2
@Service
public class OrderSeataService {
    @Resource
    private StockClientService stockClientService;

    @Resource
    private OrderDao orderDao;

    @Resource
    private OrderStockMiddleDao orderStockMiddleDao;

    /**
     * 創建訂單 Seata 強一致
     * 每筆訂單配一筆商品(庫存)
     **/
    // Transactional 第一入口加上GlobalTransactional 補償任務 Seata 會做
    // 原本的 @Transactional還是要加上
    @GlobalTransactional
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public boolean createOrderSeata(String product_name, int quantity)
            throws OkHttpGetException, NoStockException, DeductedStockQuantityException, AddOrderException, AddOrderStockMiddleException {

        log.info("Seata全局事务id=================>{}", RootContext.getXID());

        BaseResp<Stock> stockByProductName = stockClientService.getStockByProductName(product_name);
        Stock stock = RepStock(stockByProductName);

        if (stock == null) {
            throw new NoStockException();
        }

        Long id = stock.getId();

        //請求Stock服務 扣庫存 回滾只會在Order上 Stock 需要處理
        //加上GlobalTransactional 分布式事務
        boolean deductedStockQuantity = stockClientService.deductedStockQuantity(String.valueOf(id),
                String.valueOf(quantity));

        if (!deductedStockQuantity) {

            throw new DeductedStockQuantityException();
        }
        creatOrderAndMiddle(stock,quantity);
        return true;
    }

    public void creatOrderAndMiddle(Stock stock,int orderQuantity) throws AddOrderException, NoStockException, DeductedStockQuantityException,
            AddOrderStockMiddleException {
        Order order = Order
                .builder()
                .price(stock.getPrice().multiply(BigDecimal.valueOf(orderQuantity)))
                .type(1)
                .status(OrderStatusEnum.CreateIng.code)
                .create_time(new Date())
                .update_time(new Date())
                .build();
        Long addOrder = orderDao.addOrderRepId(order);
        if (addOrder == 0) {
            log.error(StatusCode.AddOrderFail.msg);
            throw new AddOrderException();
        }
        Long id = stock.getId();
        boolean deductedStockQuantity = stockClientService.deductedStockQuantityMq(String.valueOf(id),
                String.valueOf(order.getId()), String.valueOf(orderQuantity));
        if (!deductedStockQuantity) {
            throw new DeductedStockQuantityException();
        }
        //*如果沒有分布式事務 這邊報異常 Order會回滾(沒有天加訂單)
//    int sdf = 10/0;
        OrderStockMiddle orderStockMiddle = OrderStockMiddle
                .builder()
                .order_id(order.getId())
                .status(order.getStatus())
                .deducted_quantity(orderQuantity)
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




}
