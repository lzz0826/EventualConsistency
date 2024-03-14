package org.example.advice;

import lombok.extern.log4j.Log4j2;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
import org.example.exception.OkHttpGetException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OkHttpGetExceptionHandler {

  @org.springframework.web.bind.annotation.ExceptionHandler(OkHttpGetException.class)
  public BaseResp<?> handleException(OkHttpGetException ex){
    return BaseResp.fail(StatusCode.OkHttpGetFail);
  }

}
