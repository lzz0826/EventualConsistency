package test.client;

import jakarta.annotation.Resource;
import org.example.client.service.StockClientService;
import org.example.common.BaseResp;
import org.example.entities.Order;
import org.example.exception.OKHttpException;
import org.example.exception.OrderServerErrorException;
import org.junit.Test;
import test.BastTest;

public class StockClientServiceTest extends BastTest {

    @Resource
    private StockClientService stockClientService;

    @Test
    public void test() throws  OKHttpException {
        BaseResp<Order> d = stockClientService.getOrderById(88L);
        System.out.println(d);

    }







}
