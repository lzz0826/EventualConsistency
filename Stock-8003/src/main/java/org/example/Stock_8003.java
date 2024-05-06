package org.example;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


//開啟緩存管理 移至SpringCacheConfig
//@EnableCaching


@EnableRabbit
@SpringBootApplication()
public class Stock_8003 {

  public static void main(String[] args) {
    SpringApplication.run(Stock_8003.class,args);

  }

}
