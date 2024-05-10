package org.example.mq;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.StockOnDoLog;
import org.example.enums.RollbackStatusEnum;
import org.example.service.StockOnDoLogService;
import org.example.service.StockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@Slf4j
public class RollbackStockHandler {

    @Resource
    private StockService stockService;

    @Resource
    private StockOnDoLogService stockOnDoLogService;

    /**
     * 滾回數據
     * 確保操作日誌 未回滾狀態才能執行(同事務)
     * @Transactional 不能放在@RabbitListener()下 需要自己一個類不能間接調用(間接調用會無效)
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

}
