package org.example.mq;


import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.Order;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.example.config.RabbitMqConfig.Order_Release_OrderQueue_Name;


@Component
@Slf4j
@RabbitListener(queues = Order_Release_OrderQueue_Name)
public class OrderReceiver {

    @Resource
    private OrderRollbackCheck orderRollbackCheck;

    /**
     *  接收創建訂單超過付款時間
     */
    @RabbitHandler
    private void orderReceive(CheckOrderMq checkOrderMq  , Channel channel , Message msg) throws IOException {
        log.info("Received order check: {}", checkOrderMq);

        orderRollbackCheck.orderCheck(checkOrderMq,channel ,msg);

        System.out.println(checkOrderMq.toString());
        System.out.println("-----------------");
        System.out.println(msg.getMessageProperties().toString());

        //TODO檢查訂單狀態 為支付改為 失敗

        channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);

    }



}
