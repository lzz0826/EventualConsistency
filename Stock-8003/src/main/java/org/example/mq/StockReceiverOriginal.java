//package org.example.mq;
//
//
//import com.rabbitmq.client.Channel;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.example.client.service.StockClientService;
//import org.example.entities.Order;
//import org.example.entities.StockOnDoLog;
//import org.example.enums.OperationTypeEnum;
//import org.example.enums.RollbackStatusEnum;
//import org.example.exception.OKHttpException;
//import org.example.exception.OrderServerErrorException;
//import org.example.service.StockOnDoLogService;
//import org.example.service.StockService;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitHandler;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Component;
//import java.io.IOException;
//import java.util.Date;
//
//import static org.example.client.service.StockClientService.RepOrder;
//import static org.example.config.RabbitMqConfig.*;
//
//@Component
//@Slf4j
//@RabbitListener(queues = Stock_Release_Stock_Queue_Name)
//public class StockReceiverOriginal {
//    @Resource
//    private StockOnDoLogService stockOnDoLogService;
//    @Resource
//    private StockClientService stockClientService;
//
//    @Resource
//    private StockService stockService;
//
//    @Resource
//    private RabbitTemplate rabbitTemplate;
//
//    /**
//     * 檢查
//     * ---
//     * 不需要回滾:
//     * 1.訂單確認支付後 確定庫存可以後不需要補償
//     * ---
//     * 需要回滾:
//     * 1.訂單創建時打完扣Stock API後是否有回滾 訂單回滾庫存需要回滾補償
//     * 2.訂單創建完後超時為支付 庫存需要回滾(需要打訂單API)
//     * ---
//     * 返回隊列:
//     * 1.訂單服務連不到
//     * 2.訂單狀態為確認
//     * 3.其他因素
//     */
//    @RabbitHandler
//    private void stockReceiver(CheckStockMq checkStock , Channel channel , Message msg) throws IOException {
//        System.out.println("收到庫存檢查:"+checkStock);
//
//        Long stockUndoLogId = checkStock.getStock_undo_log_id();
//        Long stockId = checkStock.getStock_id();
//        Long orderId = checkStock.getOrder_id();
//
//        StockOnDoLog stockOnDoLog = null;
//
//        if (stockUndoLogId != null) {
//            stockOnDoLog = stockOnDoLogService.getStockOnDoLogDaoById(stockUndoLogId);
//        } else if (stockId != null && orderId != null) {
//            stockOnDoLog = stockOnDoLogService.getStockOnDoLogDaoByStockIdAndOrderId(stockId, orderId);
//        } else {
//            log.error("Not find stockOnDoLog");
//        }
//
//        //判斷是否要重新排隊
//        boolean reQueue = true;
//
//        //操作日誌有 在創建庫存時沒有回滾
//        if (stockOnDoLog != null){
//            try {
//                //1.訂單創建時打完扣StockAPI後是否有回滾 訂單回滾庫存需要補償
//                Order order = RepOrder(stockClientService.getOrderById(orderId));
//                String operationType = stockOnDoLog.getOperation_type();
//                Integer rollbackStatus = stockOnDoLog.getRollback_status();
//                if (order != null){
////                定單狀態 Fail = 0, Success=1  ,CreateIng = 2 , PayIng = 3
//                    switch (order.getStatus()){
//                        case 1:
//                            //訂單已正常支付無需回滾 更新庫存日誌
//                            StockOnDoLog newStockLog = StockOnDoLog
//                                    .builder()
//                                    .id(stockOnDoLog.getId())
//                                    .status(1)
//                                    .build();
//                            stockOnDoLogService.updateStockOnDoLog(newStockLog);
//                            reQueue = false;
//                            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
//                            break;
//                        case -1:
//                            //訂單支付失敗需回滾 更新庫存日誌
//                            rollback(stockOnDoLog.getStock_id(),stockOnDoLog.getQuantity(),stockOnDoLog.getId());
//                            reQueue = false;
//                            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
//                            break;
//                    }
//                    //查無訂單 訂單回滾 庫存也需要回滾   操作: 扣除 操作狀態: 等待
//                }else if (OperationTypeEnum.Decrease.name.equals(operationType) && rollbackStatus == 0){
//                    rollback(stockOnDoLog.getStock_id(),stockOnDoLog.getQuantity(),stockOnDoLog.getId());
//                    reQueue = false;
//                    channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
//                }
//            }catch (OKHttpException | OrderServerErrorException e){
//                //打訂單服務時失敗 重新排隊
//                log.error(e.getMessage());
//                reQueue = true;
//            }
//        }else {
//            //庫存操作沒有 在扣庫存時就本地回滾了 無需處理
//            reQueue = false;
//            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
//        }
//        //其他未知狀況放回隊列
//        if (reQueue){
////        channel.basicReject(msg.getMessageProperties().getDeliveryTag(),true);  // 放回原隊列 *會多次重複執行
//            //通知MQ 重新徘到死信隊列裏等待下次檢查(使用最一開始傳進來的參數)
//            CheckStockMq checkStockMq = CheckStockMq
//                    .builder()
//                    .stock_undo_log_id(stockUndoLogId)
//                    .stock_id(stockId)
//                    .order_id(orderId)
//                    .build();
//            rabbitTemplate.convertAndSend(Stock_Event_Exchange,Stock_Locked_Key,checkStockMq);
//            channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
//        }
//    }
//
//    private void rollback(Long stockId,int quantity,Long stockOnDoLogId ){
//        stockService.increaseQuantity(stockId,quantity);
//        //更新庫存日誌
//        StockOnDoLog newStockLog = StockOnDoLog
//                .builder()
//                .id(stockOnDoLogId)
//                .status(-1)
//                .rollback_status(RollbackStatusEnum.IsRollback.code)
//                .rollback_time(new Date())
//                .build();
//        stockOnDoLogService.updateStockOnDoLog(newStockLog);
//    }
//
//
//}
