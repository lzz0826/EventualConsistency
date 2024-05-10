package org.example.mq;


import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.example.mq.MqStaticResource.Stock_Release_Stock_Queue_Name;

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
