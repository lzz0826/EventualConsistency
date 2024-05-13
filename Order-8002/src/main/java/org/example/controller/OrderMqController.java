package org.example.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.common.BaseResp;
import org.example.controller.rep.CreateOrderMqReq;
import org.example.controller.rep.SuccessPayOrderMqReq;
import org.example.exception.AddOrderException;
import org.example.exception.AddOrderStockMiddleException;
import org.example.exception.DeductedStockQuantityException;
import org.example.exception.NoStockException;
import org.example.service.OrderMqService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class OrderMqController {

    @Resource
    private OrderMqService orderMqService;

    /**
     * 創建訂單 Mq 最終一致
     **/
    @PostMapping("/createOrderMq")
    public BaseResp<String> createOrderMq(@RequestBody @Valid CreateOrderMqReq req)
            throws NoStockException, DeductedStockQuantityException, AddOrderException, AddOrderStockMiddleException {

        boolean order = orderMqService.createOrderMq(req.getProduct_quantity());

        if(order){
            return BaseResp.ok("成功");
        }
        return BaseResp.ok("失敗");
    }


    /**
     * 創建訂單 Mq 最終一致
     **/
    @PostMapping("/successPayOrderMq")
    public BaseResp<String> successPayOrderMq(@RequestBody @Valid SuccessPayOrderMqReq req) {




        if(true){
            return BaseResp.ok("成功");
        }
        return BaseResp.ok("失敗");
    }







}
