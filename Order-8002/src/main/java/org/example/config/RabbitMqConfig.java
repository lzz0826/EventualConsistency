package org.example.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class RabbitMqConfig {

    /**
     *  订单与库存
     *  @Been
     *  容器中的 Binding Queue Exchange 都會自動創建
     */

    //延遲隊列 (死信后会交给 orderReleaseOrderQueue)
    private static final String Order_Delay_Queue_Name = "order.delay.queue" ;
    private static final int Order_Delay_Queue_Ttl = 60000;
    @Bean
    public Queue orderDelayQueue(){


        HashMap<String, Object> arguments = new HashMap<>();
        //死信後交付的交換機
        arguments.put("x-dead-letter-exchange",Order_Event_Exchange);
        //死信後綁定routing-key
        arguments.put("x-dead-letter-routing-key",Order_Release_Order_Key);
        //每個消息的過期時間(超過會丟棄 或是配置死信)
        arguments.put("x-message-ttl",Order_Delay_Queue_Ttl);
        //String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments

        return new Queue(
                Order_Delay_Queue_Name,
                true,
                false,
                false,
                arguments);
    }

    //訂單發布隊列
    private static final String Order_Release_OrderQueue_Name = "order.release.order.queue" ;

    @Bean
    public Queue orderReleaseOrderQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
        return new Queue(
                Order_Release_OrderQueue_Name,
                true,
                false,
                false,
                null);
    }

    //事件交换机
    private static final String Order_Event_Exchange = "order-even-exchange" ;
    @Bean
    public Exchange orderEventExchange(){
//        String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange(Order_Event_Exchange,true,false);
    }

    //創建訂單綁定
    private static final String Order_Create_Order_Key = "order.create.order" ;

    @Bean
    public Binding orderCreateOrder(){
//         @Nullable String destination, DestinationType destinationType,
//         String exchange, @Nullable String routingKey, @Nullable Map<String, Object> arguments
        return new Binding(
                //目的地 Delay_Queue
                Order_Delay_Queue_Name,
                Binding.DestinationType.QUEUE,
                Order_Event_Exchange,
                Order_Create_Order_Key,
                null);
    }

    //發布訂單綁定
    private static final String Order_Release_Order_Key = "order.release.order" ;

    @Bean
    public Binding orderReleaseOrder(){
//         @Nullable String destination, DestinationType destinationType,
//         String exchange, @Nullable String routingKey, @Nullable Map<String, Object> arguments
        return new Binding(
                //目的地 Release_OrderQueue
                Order_Release_OrderQueue_Name,
                Binding.DestinationType.QUEUE,
                Order_Event_Exchange,
                Order_Release_Order_Key,
                null);
    }











}
