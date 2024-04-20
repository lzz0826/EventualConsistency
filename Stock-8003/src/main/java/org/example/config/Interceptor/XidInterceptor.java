package org.example.config.Interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import io.seata.core.context.RootContext;

/**
 * XidInterceptor 用於在 HTTP 請求進行前後處理，確保全局事務ID（XID）被正確傳遞和管理。
 * OkHttp 不自动集成 Seata 的事务传播机制。Seata 通常与 Spring Cloud 集成时，依赖于 Spring Cloud 的通信机制
 * （如 Feign、Ribbon 等）来自动传递 XID。使用 OkHttp 的话，需要手动将 XID 从事务发起方传到被调用方。
 */
@Component
public class XidInterceptor implements HandlerInterceptor {

  /**
   * 在請求處理之前執行，用於綁定 XID 到 RootContext。
   *
   * @param request 用來獲取當前 HTTP 請求
   * @param response 用來設置 HTTP 響應
   * @param handler 選擇用於處理請求的處理器
   * @return boolean 返回 true 繼續處理請求，返回 false 則中斷請求
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    // 從 HTTP 請求頭中獲取 XID
    String xid = request.getHeader("XID");

    // 檢查 XID 是否存在，如果存在，將其綁定到當前執行的上下文中
    if (xid != null && !xid.isEmpty()) {
      RootContext.bind(xid);
    }

    // 繼續執行後續的處理流程
    return true;
  }

  /**
   * 請求完成後執行，用於清除 RootContext 中的 XID。
   *
   * @param request 用來獲取當前 HTTP 請求
   * @param response 用來設置 HTTP 響應
   * @param handler 選擇用於處理請求的處理器
   * @param ex 用來接收過程中產生的異常
   */
  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    // 無論請求處理的結果如何，完成後都解綁 XID
    RootContext.unbind();
  }
}
