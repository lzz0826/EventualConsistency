package org.example.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.example.entities.Dept;
import org.example.enums.RedisKeyEnum;
import org.example.service.RedissonService;
import org.example.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.example.dao.DeptDao;
import org.example.service.DeptService;
import org.springframework.util.StringUtils;

@Service
public class DeptServiceImpl implements DeptService {


  @Resource
  private RedisTemplate<String,String> redisTemplate;

//  @Resource
  private RedissonService redissonService;


  @Autowired
  private DeptDao dao;

  @Override
  public boolean add(Dept dept) {
    return dao.addDept(dept);
  }

  /*
  * 更新的時候要注意緩存與DB一致性
  * */
  @Override
  public int update(Dept dept) {

    //TODO 同時修改緩存的值
    //TODO 刪除緩存 reids.del("key") 等待下次更新緩存

    System.out.println("更新數據庫...");
    return dao.updateDept(dept);
  }

  @Override
  public Dept get(Long id) {
    return dao.findById(id);
  }

  @Override
  public List<Dept> list() {
    System.out.println("查詢數據庫...");
      List<Dept> list = dao.findAll();
      return list;
  }

  //TODO 產生內存溢出 OutOfDirectMemoryError
  //1.springboot2.0後使用Lettuce操作Redis客戶端 他使用Netty近行網路通信
  //2.Lettuce的bug導致Netty堆外內存溢出 -Xmx300m; Netty如果沒有指定堆外內存 默認使用 -Xmx300m
  //  可通做 -Dio.netty.maxDirectMemory進行設置
  //解決方法:
  // 不能使用-Dio.netty.maxDirectMemory只去調大堆外內存
  // 1.升級Lettuce客戶端 2.切換使用jedis
  // redisTemplate : lettuce.jedis操作底層客戶端 Spring 再次封裝redisTemplate;
  public List<Dept> getCashByList() {

    String cashKeyName = RedisKeyEnum.GetCashByListCache.keyName;

    String s = redisTemplate.opsForValue().get(cashKeyName);
    if (StringUtils.isEmpty(s)) {
      System.out.println("緩存沒命中 進到緩存");

      //本地鎖
//      List<Dept> list = localLockList(keyName);

      //分布式鎖  普通REDIS
//      List<Dept> list = redisLockList(cashKeyName);

      //分布式鎖  普通Redisson
      List<Dept> list = redissonService.redissonLockList(cashKeyName,"11");



      if (list.isEmpty()) {
        //1.空結果緩存 : 緩存穿透
        redisTemplate.opsForValue().set(cashKeyName, "[]", RandomUtil.RandomExpireTime(), TimeUnit.SECONDS);
        // 返回一個空的 ArrayList
        return new ArrayList<>();
      }
      return list;
    }
    System.out.println("緩存命中 直接返回");
    //使用TypeReference<>{} 隱式函數轉換
    List<Dept> o = JSON.parseObject(s, new TypeReference<List<Dept>>() {});
    return o;
  }

  //使用本地鎖
  public List<Dept> localLockList(String checkKeyName) {
    // synchronized (this) SpringBoot 所有組件在重器中都是單例的 用當前類來當鎖可以
    //3加鎖 : 解決緩存擊穿
    //本地鎖 synchronized JUC(Lock) 在分佈式下不能全部擋道 有幾台服務DB就會有幾次請求 可以擋下大部分
    synchronized (this){
      //得到鎖後 再去緩群確認
      String s = redisTemplate.opsForValue().get(checkKeyName);
      if(!StringUtils.isEmpty(s)){
        //緩存不為空時返回
        List<Dept> o = JSON.parseObject(s, new TypeReference<List<Dept>>() {});
        return o;
      }
      List<Dept> list = list();
      String jsonString = JSON.toJSONString(list);
      //2.設置過期時間(加上隨機值) : 緩存雪崩
      redisTemplate.opsForValue().set(checkKeyName, jsonString, RandomUtil.RandomExpireTime(), TimeUnit.SECONDS);
      return list;
    }
  }

  //使用分布式鎖 ** 加鎖和解鎖都要確保原子性 加鎖:使用setIfAbsent()Redis NX + EX  解鎖:使用Lua腳本
  public List<Dept> redisLockList(String cashKeyName) {
    String lockKey = RedisKeyEnum.GetCashByListLock.keyName;
    //賦予每個鎖值有自己的uuid(避免業務超時下個線程誤刪上個業務的鎖)
    String uuid = UUID.randomUUID().toString();
    // 加鎖：解決緩存擊穿 設置過期時間(防止沒有正常釋放鎖 須考慮業務時間)必須跟加鎖時原子抄作 Redis NX + EX
    Boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid,300,TimeUnit.SECONDS);

    if (isLock) {
      System.out.println("獲取鎖成功");
      // 再去緩存確認
      String s = redisTemplate.opsForValue().get(cashKeyName);
      if (!StringUtils.isEmpty(s)) {
        // 緩存不為空時返回
        return JSON.parseObject(s, new TypeReference<List<Dept>>() {});
      }

      List<Dept> list;
      //主要業務邏輯 **處理業務時間過長解決 : 設置長點的過期時間 在處理業務的時候無論成功或失敗都解鎖 或續期
      try {
        list = list(); //主要業務邏輯
        String jsonString = JSON.toJSONString(list);
        // 設置過期時間（加上隨機值）：緩存雪崩
        redisTemplate.opsForValue().set(cashKeyName, jsonString, RandomUtil.RandomExpireTime(), TimeUnit.SECONDS);
      }finally {
        //     釋放鎖 需要先判斷是不是自己得鎖才能刪 刪除和判斷無原子性
//      redisTemplate.delete(lockKey);
//      String lockValue = redisTemplate.opsForValue().get(lockKey);
//      if(lockValue.equals(lockValue)){
//        redisTemplate.delete(lockKey);
//      }

        //使用Lua腳本確保刪除和判斷有原子性
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then "
            + "    return redis.call('del', KEYS[1]) "
            + "else "
            + "    return 0 "
            + "end";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        List<String> keys = Collections.singletonList(lockKey);
        //使用原子性刪除鎖 返回的值不需要處理result 1 = 有刪到 0 = 沒刪到
        Long result = redisTemplate.execute(redisScript, keys, uuid);
      }
      return list;
    } else {
      System.out.println("獲取鎖失敗，自旋重試");
      // 注意：應該考慮自旋超時或者重試次數限制，避免無限遞歸
      // 可以設置自旋等待時間，避免無限自旋
      try {Thread.sleep(2000);} catch (InterruptedException e) {throw new RuntimeException(e);}
      // 這裏簡單地使用遞歸調用，可以根據需要改進為循環方式
      return redisLockList(cashKeyName);
    }
  }


}
