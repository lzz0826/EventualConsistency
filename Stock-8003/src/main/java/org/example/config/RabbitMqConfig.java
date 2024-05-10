package org.example.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

import static org.example.mq.MqStaticResource.*;

@Configuration
public class RabbitMqConfig {
    /**
     *  使用JSON序列化機制 進行消息轉換
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    public static final int Stock_Delay_Queue_Ttl = 120000;

    @Bean
    public Queue stockDelayQueue(){
        HashMap<String, Object> arguments = new HashMap<>();
        //死信後交付的交換機
        arguments.put("x-dead-letter-exchange",Stock_Event_Exchange);
        //死信後綁定routing-key
        arguments.put("x-dead-letter-routing-key",Stock_Release_Key);
        //每個消息的過期時間(超過會丟棄 或是配置死信)
        arguments.put("x-message-ttl",Stock_Delay_Queue_Ttl);
        return new Queue(Stock_Delay_Queue_Name,true,false,false,arguments);

    }

    @Bean
    public Queue stockReleaseStockQueue(){
        return new Queue(Stock_Release_Stock_Queue_Name,true,false,false);

    }

    @Bean
    public Exchange stockEventExchange(){
//        String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange(Stock_Event_Exchange,true,false);

    }


    @Bean
    public Binding stockLockedBinding(){
        return new Binding(
                Stock_Delay_Queue_Name,
                Binding.DestinationType.QUEUE,
                Stock_Event_Exchange,
                Stock_Locked_Key,
                null
        );
    }


    @Bean
    public Binding stockReleaseBinding(){
//        @Nullable Queue lazyQueue, @Nullable String destination, DestinationType destinationType,
//        String exchange, @Nullable String routingKey, @Nullable Map<String, Object> arguments
        return new Binding(
                Stock_Release_Stock_Queue_Name,
                Binding.DestinationType.QUEUE,
                Stock_Event_Exchange,
                Stock_Release_Key,
                null);
    }








}
