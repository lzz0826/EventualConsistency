package org.example.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.common.BaseResp;
import org.example.controller.rep.CreateOrderMqReq;
import org.example.controller.rep.FailPayOrderMqReq;
import org.example.controller.rep.SuccessPayOrderMqReq;
import org.example.exception.*;
import org.example.service.NotFoundUpdateOrderException;
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
    public BaseResp<Long> createOrderMq(@RequestBody @Valid CreateOrderMqReq req)
            throws NoStockException, DeductedStockQuantityException, AddOrderException, AddOrderStockMiddleException, NotFoundOrderException {

        Long orderId = orderMqService.createOrderMq(req.getProduct_quantity());

        return BaseResp.ok(orderId);
    }


    /**
     * 修改訂單 Mq 最終一致
     * 更新訂單狀態 PayIng -> Success (訂單 和 訂單中間表)
     * 更新多筆訂單 包含中間表 和訂單表 多對多
     **/
    @PostMapping("/successPayOrderMq")
    public BaseResp<String> successPayOrderMq(@RequestBody @Valid SuccessPayOrderMqReq req) throws NotFoundOrderException, NotFoundUpdateOrderException {
        boolean b = orderMqService.updateOrderStatusToSuccess(req.getOrderId());
        if(b){
            return BaseResp.ok("成功");
        }
        return BaseResp.ok("失敗");
    }

    /**
     * 修改訂單 Mq 最終一致
     * 更新訂單狀態 PayIng -> Fail (訂單 和 訂單中間表)
     * 更新多筆訂單 包含中間表 和訂單表 多對多
     **/
    @PostMapping("/failPayOrderMq")
    public BaseResp<String> failPayOrderMq(@RequestBody @Valid FailPayOrderMqReq req) throws NotFoundOrderException, NotFoundUpdateOrderException {
        boolean b = orderMqService.updateOrderStatusToFail(req.getOrderId());
        if(b){
            return BaseResp.ok("成功");
        }
        return BaseResp.ok("失敗");
    }







}
