package org.example.mq;


import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.client.service.StockClientService;
import org.example.entities.Order;
import org.example.entities.Stock;
import org.example.entities.StockOnDoLog;
import org.example.enums.OperationTypeEnum;
import org.example.enums.RollbackStatusEnum;
import org.example.exception.OKHttpException;
import org.example.exception.OrderServerErrorException;
import org.example.service.StockOnDoLogService;
import org.example.service.StockService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;

import static org.example.client.service.StockClientService.RepOrder;
import static org.example.config.RabbitMqConfig.*;

@Component
@Slf4j
@RabbitListener(queues = Stock_Release_Stock_Queue_Name)
public class StockReceiver {

    @Resource
    private StockRollbackCheck stockRollbackCheck;

    /**
     * 檢查
     * ---
     * 不需要回滾:
     * 1.訂單確認支付後 確定庫存可以後不需要補償
     * ---
     * 需要回滾:
     * 1.訂單創建時打完扣Stock API後是否有回滾 訂單回滾庫存需要回滾補償
     * 2.訂單創建完後超時為支付 庫存需要回滾(需要打訂單API)
     * ---
     * 返回隊列:
     * 1.訂單服務連不到
     * 2.訂單狀態為確認
     * 3.其他因素
     */
    @RabbitHandler
    private void stockReceiver(CheckStockMq checkStock , Channel channel , Message msg) throws IOException {
        log.info("Received stock check: {}", checkStock);
        stockRollbackCheck.stockCheck(checkStock,channel,msg);
    }

}
