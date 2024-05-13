package org.example.mq;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.Order;
import org.example.entities.middle.OrderStockMiddle;
import org.example.enums.OrderStatusEnum;
import org.example.enums.OrderStockMiddleStatusEnum;
import org.example.service.OrderService;
import org.example.service.OrderStockMiddleService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static org.example.config.RabbitMqConfig.*;
import static org.example.mq.MqStaticResource.*;


@Component
@Slf4j
public class OrderRollbackCheck {
    /**
     * 接收創建訂單超過付款時間
     *
     *  需回滾:
     *  1.查無訂單.訂單消失(本地回滾無需處理) 通知Stock服務回滾庫存
     *  2.訂單付款失敗(更新 訂單.中間表 狀態) 通知Stock服務 回滾庫存
     *
     *  不需回滾:
     *  1.訂單成功(更新 訂單.中間表 狀態)
     *
     *  放回隊列:
     *  1.其他狀況
     *
     */
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderStockMiddleService orderStockMiddleService;

    public void orderCheck(CheckOrderMq checkOrderMq  , Channel channel , Message msg) throws IOException {
        Order orderById = orderService.getOrderById(checkOrderMq.getOrder_id());

        //有訂單 檢查訂單 確認訂單超時
        if (orderById != null){
            handleOrderOperation(orderById,channel,msg);
        }else {
            //沒有訂單 本地已經回滾過
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }

    }


    /**
     * 確認訂單狀態
     */
    private void handleOrderOperation(Order order,Channel channel,Message msg) throws IOException {
        boolean reQueue = true;
        try {
            switch (order.getStatus()){
                //定單狀態 Fail = -1, Success= 1  ,CreateIng = 0 , PayIng = 2
                case 1  :
                    //訂單成功 更新中間表 TODO 可以再用 Queues通知其他 服務做事 例:積分點累計
                    updateOrderStock(order.getId(),OrderStockMiddleStatusEnum.Success.code);
                    channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                    reQueue = false;
                    break;
                case 0 :
                case 2 :
                    //訂單付超時 更新訂單 中間表 狀態 Fail 通知庫存回滾(已有查單給庫存做回滾) 雙邊確認
                    updateOrder(order.getId(), OrderStatusEnum.Fail.code);
                    updateOrderStock(order.getId(),OrderStockMiddleStatusEnum.Fail.code);
                    notifyStockRollback(order.getId());
                    channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                    reQueue = false;
                    break;
                case -1 :
                    //訂單已取消 本地不用操作 通知庫存回滾(已有查單給庫存做回滾) 雙邊確認
                    //TODO 手動更新訂單(用戶支付 或取消API 當下就需要更新訂單和中間表)
                    notifyStockRollback(order.getId());
                    reQueue = false;
                    channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                    break;
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }

        //其他未知狀況放回隊列
        if (reQueue) {
            reQueueMessage(order, channel, msg);
        }

    }

    /**
     * 更新 訂單表
     */
    private void updateOrder(Long orderId,int status) {
        orderService.updateOrderStatus(orderId,status);
    }


    /**
     * 更新 訂單中間表
     */
    private void updateOrderStock(Long orderId,int status) {
        orderStockMiddleService.updateOrderStatusByOrderId(orderId,status);
    }



    /**
     * 通知 Stock滾回數據 (雙邊驗證)
     * 通過MQ 像所有綁定Order_Release_Other_Key的隊列發送回滾消息(需要回滾的所有服務)
     */
    public void notifyStockRollback(Long orderId) {
        List<OrderStockMiddle> orderStockMiddles = orderStockMiddleService.findOrderId(orderId);
        for (OrderStockMiddle orderStockMiddle : orderStockMiddles) {
            CheckStockMq checkStockMq = CheckStockMq
                    .builder()
                    .stock_id(orderStockMiddle.getStock_id())
                    .order_id(orderId)
                    .build();
            rabbitTemplate.convertAndSend(Order_Event_Exchange,Order_Release_Other_Key,checkStockMq);
        }
    }

    /**
     * 重新放回隊列 等待下次檢查
     */
    private void reQueueMessage(Order order, Channel channel, Message msg) throws IOException {
        CheckOrderMq checkOrderMq = CheckOrderMq
                .builder()
                .order_id(order.getId())
                .build();
        rabbitTemplate.convertAndSend(Order_Event_Exchange,Order_Create_Order_Key,checkOrderMq);
        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    }


}
