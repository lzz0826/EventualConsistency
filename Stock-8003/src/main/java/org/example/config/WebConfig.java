package org.example.config;

import jakarta.annotation.Resource;
import org.example.config.Interceptor.XidInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Resource
  private XidInterceptor xidInterceptor;

  /**
   * 重写 addInterceptors 方法来注册自定义拦截器。
   * @param registry 拦截器注册表，允许添加拦截器以在请求到达控制器前进行预处理和后处理。
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 将 XidInterceptor 添加到应用程序的拦截器注册表中。
    // 这确保 XidInterceptor 将在请求到达控制器之前进行拦截和处理。
    registry.addInterceptor(xidInterceptor);
//    registry.addInterceptor(anotherInterceptor);  // 添加另一个拦截器

  }
}
