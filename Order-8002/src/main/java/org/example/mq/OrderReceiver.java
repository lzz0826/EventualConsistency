package org.example.mq;


import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.example.mq.MqStaticResource.Order_Release_OrderQueue_Name;


@Component
@Slf4j
@RabbitListener(queues = Order_Release_OrderQueue_Name)
public class OrderReceiver {

    @Resource
    private OrderCheck orderCheck;

    /**
     *  接收創建訂單超過付款時間
     */
    @RabbitHandler
    private void orderReceive(CheckOrderMq checkOrderMq  , Channel channel , Message msg) throws IOException {
        log.info("Received order check: {}", checkOrderMq);
        orderCheck.orderCheck(checkOrderMq,channel ,msg);
    }



}
