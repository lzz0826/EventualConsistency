package org.example.advice;


import lombok.extern.log4j.Log4j2;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class SystemExceptionHandler {
  /**
   * 系统异常
   */
  @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
  public BaseResp<?> handleException(Exception e) {
    e.printStackTrace();
    return BaseResp.fail(StatusCode.SystemError);
  }

}
