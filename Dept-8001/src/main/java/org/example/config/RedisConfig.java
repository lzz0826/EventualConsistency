package org.example.config;


import java.time.Duration;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.database}")
    private int database;

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.timeout}")
    private long timeout;

    @Value("${spring.redis.lettuce.shutdown-timeout}")
    private long shutDownTimeout;

    @Value("${spring.redis.lettuce.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.lettuce.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.lettuce.pool.max-wait}")
    private long maxWait;

    @Bean // 將返回的對象作為Spring Bean管理
    LettuceConnectionFactory redisConnectionFactory() {
        // 創建Redis連接工廠
        GenericObjectPoolConfig<?> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxIdle(maxIdle); // 設置最大閒置連接數
        genericObjectPoolConfig.setMinIdle(minIdle); // 設置最小閒置連接數
        genericObjectPoolConfig.setMaxTotal(maxActive); // 設置最大活動連接數
        genericObjectPoolConfig.setMaxWait(Duration.ofMillis(maxWait)); // 設置最大等待時間
        genericObjectPoolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(100)); // 設置驅逐運行間隔
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setDatabase(database); // 設置Redis數據庫索引
        redisStandaloneConfiguration.setHostName(host); // 設置Redis主機名
        redisStandaloneConfiguration.setPort(port); // 設置Redis端口
        redisStandaloneConfiguration.setPassword(RedisPassword.of(password)); // 設置Redis密碼
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(timeout)) // 設置命令超時
            .shutdownTimeout(Duration.ofMillis(shutDownTimeout)) // 設置關閉超時
            .poolConfig(genericObjectPoolConfig) // 設置連接池配置
            .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig); // 創建Lettuce連接工廠
        factory.setShareNativeConnection(true); // 共享本地連接
        factory.setValidateConnection(false); // 驗證連接
        return factory; // 返回連接工廠
    }


    @Bean // 將返回的對象作為Spring Bean管理
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 創建RedisTemplate
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory); // 設置連接工廠

        // 配置序列化器
        StringRedisSerializer keySerializer = new StringRedisSerializer(); // 創建key的序列化器
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(); // 創建value的序列化器

        template.setKeySerializer(keySerializer); // 設置key的序列化器
        template.setValueSerializer(valueSerializer); // 設置value的序列化器
        template.setHashKeySerializer(keySerializer); // 設置hash key的序列化器
        template.setHashValueSerializer(valueSerializer); // 設置hash value的序列化器
        template.afterPropertiesSet(); // 初始化template

        return template; // 返回RedisTemplate
    }

}
