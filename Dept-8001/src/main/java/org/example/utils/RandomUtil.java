package org.example.utils;


import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RandomUtil {


  public static long RandomExpireTime(){
    Random randomNumbers = new Random();
    int i = randomNumbers.nextInt(10);

    // 2.設置過期時間(加上隨機值) : 緩存雪崩
    long expireTime = TimeUnit.SECONDS.toSeconds(10 + i);

    return expireTime;
  }

}
