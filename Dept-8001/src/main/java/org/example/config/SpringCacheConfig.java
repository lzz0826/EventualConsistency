package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;


// 啟用對特定配置屬性類型的支持 可使用  @Autowired 或是帶到參數內
@EnableConfigurationProperties(CacheProperties.class)
//開啟緩存管理
@EnableCaching
@Configuration
public class SpringCacheConfig {

//  @Autowired
//  CacheProperties cacheProperties ;

  @Bean
  RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){

    // 使用默認的緩存配置並進行修改
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

    // 將緩存的鍵序列化為字符串
    config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));

  // 將緩存的值序列化為 JSON 格式
    config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

    /*
    * 使用配置文件
    *
    * 1.原本的配置文件保定的配置類
    *    @ConfigurationProperties(prefix = "spring.cahe")
    *    public class CacheProperties
    * 2.要上它生效
    * @EnableConfigurationProperties(CacheProperties.class)
    *
    * */

    // 從CacheProperties中獲取Redis相關配置信息
    Redis redisProperties = cacheProperties.getRedis();

    // 如果設置了緩存的存活時間（TTL），則將RedisCacheConfiguration中的存活時間設置為相應的值
    if(redisProperties.getTimeToLive() != null){
      config = config.entryTtl(redisProperties.getTimeToLive());
    }

    // 如果設置了緩存的鍵前綴，則將RedisCacheConfiguration中的鍵前綴設置為相應的值
    if(redisProperties.getKeyPrefix() != null){
      config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
    }

    // 如果未設置緩存空值，則禁用RedisCacheConfiguration中的緩存空值
    if(!redisProperties.isCacheNullValues()){
      config = config.disableCachingNullValues();
    }

    // 如果未使用鍵前綴，則禁用RedisCacheConfiguration中的鍵前綴
    if(!redisProperties.isUseKeyPrefix()){
      config = config.disableKeyPrefix();
    }

    return config;
  }


}














