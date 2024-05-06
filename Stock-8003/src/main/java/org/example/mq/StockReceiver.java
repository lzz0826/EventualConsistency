package org.example.mq;


import com.rabbitmq.client.Channel;
import org.example.entities.Order;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RabbitListener()
public class StockReceiver {

    @RabbitHandler
    private void stockReceiver(Order order , Channel channel , Message msg) throws IOException {

        System.out.println(order.toString());
        System.out.println("-----------------");
        System.out.println(msg.getMessageProperties().toString());


    }



}
