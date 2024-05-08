package test.mapper;

import jakarta.annotation.Resource;
import org.example.dao.StockOnDoLogDao;
import org.example.entities.StockOnDoLog;
import org.junit.Test;
import test.BastTest;

public class StockOnDoLogDaoMapperTest extends BastTest {



    @Resource
    private StockOnDoLogDao dao;


    @Test
    public void testFindByStockIdAndOrderId(){
        StockOnDoLog byStockIdAndOrderId = dao.findByStockIdAndOrderId(22L,33L);
        System.out.println(byStockIdAndOrderId);

    }

}
