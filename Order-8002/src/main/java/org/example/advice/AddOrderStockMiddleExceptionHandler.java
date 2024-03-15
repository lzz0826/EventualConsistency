package org.example.advice;

import lombok.extern.log4j.Log4j2;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
import org.example.exception.AddOrderException;
import org.example.exception.AddOrderStockMiddleException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Log4j2
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AddOrderStockMiddleExceptionHandler {

  @org.springframework.web.bind.annotation.ExceptionHandler(AddOrderStockMiddleException.class)
  public BaseResp<?> handleException(AddOrderStockMiddleException ex){
    return BaseResp.fail(StatusCode.AddOrderStockMiddleFail);
  }
}
