package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

//開啟緩存管理 移至SpringCacheConfig
//@EnableCaching
@SpringBootApplication()
public class Dept_8001 {

  public static void main(String[] args) {
    SpringApplication.run(Dept_8001.class,args);

  }

}
