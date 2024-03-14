package org.example.advice;

import com.alibaba.fastjson2.JSONException;
import lombok.extern.log4j.Log4j2;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JSONExceptionExceptionHandler {

  @org.springframework.web.bind.annotation.ExceptionHandler(JSONException.class)
  public BaseResp<?> handleException(JSONException ex){
    return BaseResp.fail(StatusCode.JSONSerializationFail);
  }

}
