package org.example.service;

import io.seata.core.context.RootContext;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.example.dao.StockDao;
import org.example.entities.Stock;
import org.example.exception.NoStockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Log4j2
public class StockSeataService {

    @Resource
    public StockDao stockDao;

    /**
     * Seata 扣庫存 檢查庫存後 在更新(在同一段 sql執行確保原子性)
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ , rollbackFor = Exception.class)
    public boolean deductedStockQuantity(Long id , int quantity)
            throws NoStockException {

        log.info("Seata全局事务id=================>{}", RootContext.getXID());


        Stock byId = stockDao.findById(id);

        if(byId == null){
            throw new NoStockException();
        }

        Integer stockQuantity = byId.getQuantity();

        if(stockQuantity - quantity < 0 ){
            throw new NoStockException();
        }

        int b = stockDao.deductedQuantity(id,quantity,new Date());

        if(b <= 0){
            throw new NoStockException();
        }
        return true;

    }
}
