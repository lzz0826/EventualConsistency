package org.example.factory;

import java.lang.reflect.Method;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.example.dao.TestCglibDao;

public class ProxyFactory implements MethodInterceptor {


  //維護一個對象
  private Object target;


  //構造器 傳入一個被代理的對象
  public ProxyFactory(Object target){
    this.target = target;
  }

  //返回一個代理對象 是 target 對象的代理對象
  public Object getProxyInstance(){
    //1創建一個工具例
    Enhancer enhancer = new Enhancer();
    //2.設置父類
    enhancer.setSuperclass(TestCglibDao.class);
    //3.設置回調函數
    enhancer.setCallback(this);
    //4.創建紙類對象 即代理對象
    return enhancer.create();
  }

  @Override
  public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy)
      throws Throwable {
    System.out.println("Before invoking method: " + method.getName());
    Object returnVal = method.invoke(target, args);
    System.out.println("After invoking method: " + method.getName());
    return returnVal;
  }
}
