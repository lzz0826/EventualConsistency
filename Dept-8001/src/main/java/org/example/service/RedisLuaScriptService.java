package org.example.service;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;


//Lua腳本Service
@Service
public class RedisLuaScriptService {
  @Autowired
  private StringRedisTemplate stringRedisTemplate; //使用Spring 來下Lua腳本

  public Long executeLuaScriptFromString(String luaScript, String key, Object args) {
    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);

    // 準備參數
    Object[] scriptArgs = new Object[]{args};
    if (key == null || key.isEmpty()) {
      key = ""; // 如果key為null或者空字符串，則設置為一個空字符串
    }

    // 執行 Lua 腳本
    Long result = stringRedisTemplate.execute(redisScript, Collections.singletonList(key), scriptArgs);
    return result;
  }
}