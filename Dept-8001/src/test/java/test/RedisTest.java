package test;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

public class RedisTest extends BastTest {

  @Resource
  private RedisTemplate<String,String> redisTemplate;

  @Resource
  private RedissonClient redissonClient;

  @Test
  public void redissonClientTest(){
    System.out.println(redissonClient);
  }

  @Test
  public void redisTemplateTest(){

    String key = "key2";

    redisTemplate.opsForValue().set(key,"value2");

    String s = redisTemplate.opsForValue().get(key);
    System.out.println(s);
  }

  @Test
  public void RandomTest(){
    Random randomNumbers = new Random();
    for(int i = 0 ; i < 100 ; i++){
      int d = randomNumbers.nextInt(10);
      System.out.println(d);
    }
  }


}
