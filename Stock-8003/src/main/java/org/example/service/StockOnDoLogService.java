package org.example.service;

import jakarta.annotation.Resource;
import org.example.dao.StockOnDoLogDao;
import org.example.entities.StockOnDoLog;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StockOnDoLogService {

    @Resource
    private StockOnDoLogDao stockOnDoLogDao;

    public StockOnDoLog getStockOnDoLogDaoById(Long id){
        return stockOnDoLogDao.findById(id);
    }

    public StockOnDoLog getStockOnDoLogDaoByStockIdAndOrderId(Long StockId , Long OrderId){
        return stockOnDoLogDao.findByStockIdAndOrderId(StockId,OrderId);
    }

    public int updateStockOnDoLog(StockOnDoLog stockOnDoLog){
        stockOnDoLog.setUpdate_time(new Date());
        return stockOnDoLogDao.updateStockOnDoLog(stockOnDoLog);
    }


}
