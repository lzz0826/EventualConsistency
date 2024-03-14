package org.example.advice;

import lombok.extern.log4j.Log4j2;
import org.example.common.BaseResp;
import org.example.common.StatusCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MethodArgumentNotValidExceptionHandler {

  @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
  public BaseResp<?> handleException(MethodArgumentNotValidException ex){
    BindingResult bindingResult = ex.getBindingResult();
    FieldError fieldError = bindingResult.getFieldError();
    String fieldName = fieldError.getField();
    String errorMessage = fieldError.getDefaultMessage();
    return BaseResp.fail(fieldName + ": " + errorMessage,StatusCode.SystemError);
  }
}
