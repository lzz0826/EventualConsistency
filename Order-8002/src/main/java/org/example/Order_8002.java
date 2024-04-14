package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//TODO   vgroupMapping.my_test_tx_group  未解
//TODO 沒有配置Nacos報的錯 service.vgroupMapping.default_tx_group configuration item is required
//開啟緩存管理 移至SpringCacheConfig
//@EnableCaching
@SpringBootApplication()
public class Order_8002 {

  public static void main(String[] args) {
    SpringApplication.run(Order_8002.class,args);

  }

}
