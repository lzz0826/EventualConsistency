package org.example.config;


import java.io.IOException;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {


  @Value("${spring.redis.host}")
  private String redisHost;


  @Value("${spring.redis.password}")
  private String redisPassword;

  @Value("${spring.redis.port}")
  private int redisPort;

  @Value("${spring.redis.database}")
  private int database;

  //所有對Redisson的使用都是通過 RedissonClient
  @Bean(destroyMethod = "shutdown")
  public RedissonClient redisson() throws IOException {
    //1.創建配置
    Config config = new Config();
    String address = "redis://" + redisHost + ":" + redisPort;
    config.useSingleServer()
        .setAddress(address)
        .setPassword(redisPassword)
        .setDatabase(database);
    config.useSingleServer().setAddress(address);
    //2.根據Config建出RedissonClient示例
    RedissonClient redissonClient = Redisson.create(config);
    return  redissonClient;
  }









}
