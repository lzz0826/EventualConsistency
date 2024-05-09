package org.example.mq;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.client.service.StockClientService;
import org.example.entities.Order;
import org.example.entities.StockOnDoLog;
import org.example.enums.OperationTypeEnum;
import org.example.enums.RollbackStatusEnum;
import org.example.exception.OKHttpException;
import org.example.exception.OrderServerErrorException;
import org.example.service.StockOnDoLogService;
import org.example.service.StockService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;

import static org.example.client.service.StockClientService.RepOrder;
import static org.example.config.RabbitMqConfig.Stock_Event_Exchange;
import static org.example.config.RabbitMqConfig.Stock_Locked_Key;

@Component
@Slf4j
public class StockRollbackCheck {

    @Resource
    private StockOnDoLogService stockOnDoLogService;
    @Resource
    private StockClientService stockClientService;

    @Resource
    private StockService stockService;

    @Resource
    private RabbitTemplate rabbitTemplate;

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
    public void stockCheck(CheckStockMq checkStock , Channel channel , Message msg) throws IOException {
        Long stockUndoLogId = checkStock.getStock_undo_log_id();
        Long stockId = checkStock.getStock_id();
        Long orderId = checkStock.getOrder_id();

        StockOnDoLog stockOnDoLog = findStockOnDoLog(stockUndoLogId, stockId, orderId);

        //操作日誌有 在創建庫存時沒有回滾
        if (stockOnDoLog != null) {
            handleStockOperation(stockOnDoLog, channel, msg);
        } else {
            //庫存操作沒有 在扣庫存時就本地回滾了 無需處理
            log.error("StockOnDoLog not found.");
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * 匹配訂單日誌
     * 有stockUndoLogId使用stockUndoLogId查詢
     * 沒有stockUndoLogId使用 stockId + orderId 查詢
     * stockId + orderId :
     * 如果前一次order服務回滾經測試 orderId回滾自增建還是會算 沒有重複orderId的疑慮
     * 保險起 確保拿到最新的
     *
     */
    private StockOnDoLog findStockOnDoLog(Long stockUndoLogId, Long stockId, Long orderId) {
        if (stockUndoLogId != null) {
            return stockOnDoLogService.getStockOnDoLogDaoById(stockUndoLogId);
        } else if (stockId != null && orderId != null) {
            return stockOnDoLogService.getStockOnDoLogDaoByStockIdAndOrderId(stockId, orderId);
        } else {
            return null;
        }
    }

    /**
     * 確認訂單狀態
     */
    private void handleStockOperation(StockOnDoLog stockOnDoLog, Channel channel, Message msg) throws IOException {
        //判斷是否要重新排隊
        boolean reQueue = true;
        try {
            //1.訂單創建時打完扣StockAPI後是否有回滾 訂單回滾庫存需要補償
            Order order = RepOrder(stockClientService.getOrderById(stockOnDoLog.getOrder_id()));
            if (order != null) {
                switch (order.getStatus()) {
                    //定單狀態 Fail = 0, Success=1  ,CreateIng = 2 , PayIng = 3
                    case 1:
                        //訂單已正常支付無需回滾 更新庫存日誌
                        updateStockLog(stockOnDoLog, 1);
                        reQueue = false;
                        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                        break;
                    case -1:
                        //訂單支付失敗需回滾 更新庫存日誌
                        rollbackStock(stockOnDoLog);
                        reQueue = false;
                        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                        break;
                }
                //查無訂單 訂單回滾 庫存也需要回滾   操作: 扣除 操作狀態: 等待
            } else if (OperationTypeEnum.Decrease.name.equals(stockOnDoLog.getOperation_type())
                    && stockOnDoLog.getRollback_status() == 0) {
                rollbackStock(stockOnDoLog);
                reQueue = false;
                channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (OKHttpException | OrderServerErrorException e) {
            //打訂單服務時失敗 重新排隊
            log.error(e.getMessage());
        }

        //其他未知狀況放回隊列
        if (reQueue) {
            reQueueMessage(stockOnDoLog, channel, msg);
        }
    }



    /**
     * 更新庫存日誌
     */
    private void updateStockLog(StockOnDoLog stockOnDoLog, int status) {
        StockOnDoLog newStockLog = StockOnDoLog.builder()
                .id(stockOnDoLog.getId())
                .status(status)
                .build();
        stockOnDoLogService.updateStockOnDoLog(newStockLog);
    }

    /**
     * 滾回數據
     * 確保操作日誌 未回滾狀態才能執行(同事務)
     * @Transactional 不能放在@RabbitListener()下
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ , rollbackFor = Exception.class)
    protected void rollbackStock(StockOnDoLog stockOnDoLog) {
        StockOnDoLog sdl = stockOnDoLogService.getStockOnDoLogDaoById(stockOnDoLog.getId());
        if (RollbackStatusEnum.NotRollback.code == sdl.getRollback_status()){
            stockService.increaseQuantity(stockOnDoLog.getStock_id(), stockOnDoLog.getQuantity());
            StockOnDoLog newStockLog = StockOnDoLog.builder()
                    .id(stockOnDoLog.getId())
                    .status(-1)
                    .rollback_status(RollbackStatusEnum.IsRollback.code)
                    .rollback_time(new Date())
                    .build();
            stockOnDoLogService.updateStockOnDoLog(newStockLog);
        }
    }

    /**
     * 重新放回隊列 等待下次檢查
     */
    private void reQueueMessage(StockOnDoLog stockOnDoLog, Channel channel, Message msg) throws IOException {
        CheckStockMq checkStockMq = CheckStockMq.builder()
                .stock_undo_log_id(stockOnDoLog.getId())
                .stock_id(stockOnDoLog.getStock_id())
                .order_id(stockOnDoLog.getOrder_id())
                .build();
        rabbitTemplate.convertAndSend(Stock_Event_Exchange, Stock_Locked_Key, checkStockMq);
        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    }

}
