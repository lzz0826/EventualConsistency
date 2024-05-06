package test;

import jakarta.annotation.Resource;
import org.example.dao.StockDao;
import org.example.dao.StockOnDoLogDao;
import org.example.entities.Stock;
import org.example.entities.StockOnDoLog;
import org.example.enums.OperationTypeEnum;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class StockOnDoLogMapperTest extends BastTest{

  @Resource
  private StockOnDoLogDao dao;

  @Test
  public void testAddStockOnDoLog(){
    StockOnDoLog build = StockOnDoLog.builder()
            .stock_id(99L)
            .order_id(88L)
            .operation_type(OperationTypeEnum.Decrease.name)
            .quantity(1)
            .operation_time(new Date())
            .description("Test999")
            .status(1)
            .rollback_status(1)
            .rollback_time(new Date())
            .update_time(new Date())
            .create_time(new Date())
            .build();
    boolean b = dao.addStockOnDoLog(build);
    System.out.println(b);
    System.out.println(build.getId());

  }


  @Test
  public void testUpdateStockOnDoLog(){
    StockOnDoLog build = StockOnDoLog.builder()
            .id(1L)
            .stock_id(22L)
            .order_id(33L)
            .operation_type(OperationTypeEnum.Increase.name)
            .quantity(2)
            .operation_time(new Date())
            .description("Testfff")
            .status(2)
            .rollback_status(2)
            .rollback_time(new Date())
            .update_time(new Date())
            .create_time(new Date())
            .build();
    int b = dao.updateStockOnDoLog(build);
    System.out.println(b);

  }

  @Test
  public void testFindById(){
    StockOnDoLog byId = dao.findById(5L);
    System.out.println(byId);

  }




}
