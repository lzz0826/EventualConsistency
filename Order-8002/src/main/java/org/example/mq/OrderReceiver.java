package org.example.mq;


import com.rabbitmq.client.Channel;
import org.example.entities.Order;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.example.config.RabbitMqConfig.Order_Release_OrderQueue_Name;


@Component
@RabbitListener(queues = Order_Release_OrderQueue_Name)
public class OrderReceiver {

    @RabbitHandler
    private void orderReceive(Order order , Channel channel , Message msg) throws IOException, ClassNotFoundException {

        System.out.println(order.toString());
        System.out.println("-----------------");
        System.out.println(msg.getMessageProperties().toString());


        System.out.println("收到過期訂單 準備關閉訂單"+ order.getCreate_time());

        channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);

    }



}
