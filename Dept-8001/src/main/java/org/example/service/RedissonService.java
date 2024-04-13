package org.example.service;

import static org.example.enums.RedisKeyEnum.LockDoorLock;
import static org.example.enums.RedisKeyEnum.Park;
import static org.example.enums.RedisKeyEnum.RedissonLockList;
import static org.example.enums.RedisKeyEnum.RedissonWriteLock;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.example.entities.Dept;
import org.example.utils.RandomUtil;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedissonService {


  @Resource
  private RedisTemplate<String,String> redisTemplate;

  @Resource
  private RedissonClient redissonClient;

  @Resource
  private DeptService deptService;

  @Resource
  private RedisLuaScriptService luaScriptService;


  public List<Dept> redissonLock(){

    List<Dept> list = new ArrayList<>();

    //1.獲取鎖 只要鎖名一樣 就是同把鎖
    RLock redissonLock = redissonClient.getLock("redissonLock");

    //2.加鎖
    //.redissonLock.lock() 解決了: 1.業務時間過長問題: 自動給鎖續上默認30s(看門狗)
    //                            2.沒有正常釋放鎖問題: 加鎖的業務只要運行完成 就不會給當前鎖續期 默認在30s後刪除
    //redissonLock.lock(); //阻塞式等待

    //推薦: 帶入過期時間  自訂10秒解鎖 不會有看門狗自動續期(定好業物時間 省掉續期操作) 手動解鎖
    redissonLock.lock(10, TimeUnit.SECONDS);
    try {
      System.out.println("獲取鎖"+Thread.currentThread().getId());
      System.out.println("處理業務中...");
      Thread.sleep(3000);
      list = deptService.list();

      return list;

    }catch (Exception e){
      System.out.println("異常...");
    }finally {
      System.out.println("釋放鎖"+Thread.currentThread().getId());
      //3.解鎖
      redissonLock.unlock();

    }
    return list;
  }

  //讀寫所能保證讀到最新數據 修改期間 寫鎖:排他鎖(互斥鎖 獨享鎖)  讀鎖:共享鎖(相當於無鎖)
  //寫鎖沒釋放讀就必須等待
  //讀 + 讀 相當於無鎖(共享鎖) 所有當前的讀鎖 都會同時加鎖成功
  //寫 + 讀 等待寫鎖釋放
  //寫 + 寫 阻塞鎖釋放
  //讀 + 寫 有讀鎖 寫也必須等待
  //只要有寫的存在 都必須等待
  public String redissonWriteLock(){

    String uuid = "";

//  類似JUC   ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(RedissonWriteLock.keyName);

    //獲取寫鎖
    System.out.println("或取寫鎖...");

    readWriteLock.writeLock().lock();
    try {
      System.out.println("業務處理中...");
      Thread.sleep(3000);
      uuid = String.valueOf(UUID.randomUUID());
      System.out.println(uuid);
      redisTemplate.opsForValue().set("testValueKey",uuid);
    }catch (Exception e){

    }finally {
      //解鎖
      System.out.println("釋放寫鎖...");
      readWriteLock.writeLock().unlock();
    }

    return uuid;
  }

  public String redissonReadLock(){

    String uuid = "";

    RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(RedissonWriteLock.keyName);
    //獲取讀鎖
    System.out.println("獲取讀鎖...");
    readWriteLock.readLock().lock();
    try {
      uuid  = redisTemplate.opsForValue().get("testValueKey");
      System.out.println(uuid);
    }catch (Exception e){

    }finally {
      //釋放讀鎖
      System.out.println("釋放讀鎖...");
      readWriteLock.readLock().unlock();
    }

    return uuid;
  }





  //信號量也作為分布式限流
  //車庫停車 3車位
  public String parkCreat(int count){
    //    //跟JUC Semaphore 一樣
//    Semaphore semaphore = new Semaphore(5);
//    semaphore.release();//釋放一個車位
//    semaphore.acquire();//佔一個車位

    RSemaphore semaphore = redissonClient.getSemaphore(Park.keyName);
    semaphore.trySetPermits(count); // 這裡設置初始許可數量為

    return "OK Creat";
  }

  public String parkIn() throws InterruptedException {
    RSemaphore semaphore = redissonClient.getSemaphore(Park.keyName);
//    semaphore.acquire(); //佔一個車位 會阻塞
//    //可以不等直接返回
//    boolean b = semaphore.tryAcquire();
//    if(b){
//      //執行業務
//    }else {
//      return "滿了";
//    }
//
//    return "OK In";

    //使用Lua腳本確保原子性
    String luaScript = "if tonumber(redis.call('get', KEYS[1]) or 0) > 0 then\n"
        + "    if tonumber(redis.call('get', KEYS[1]) or 0) <= tonumber(ARGV[1]) then\n"
        + "        redis.call('decr', KEYS[1])\n"
        + "        return 1\n"
        + "    else\n"
        + "        return 0\n"
        + "    end\n"
        + "else\n"
        + "    return 0\n"
        + "end";

    Long integer = luaScriptService.executeLuaScriptFromString(luaScript,Park.keyName,"3");


    if(integer == 1){
      return "OK In";
    }else {
      return "滿了";
    }

  }

  public String parkOut(){
    RSemaphore semaphore = redissonClient.getSemaphore(Park.keyName);
    //如果沒有先創好 會直接在REDIS裡創建 然後+1

    //限制對大流量
    if(semaphore.availablePermits() >= 3 ){
      return "超過限制數量";
    }

    semaphore.release();//釋放一個車位
    return "OK Out";
  }




  //閉鎖 開啟紀數器
  public String lockDoor(int count) throws InterruptedException {
    RCountDownLatch countDownLatch = redissonClient.getCountDownLatch(LockDoorLock.keyName);
//    CountDownLatch countDownLatch = new CountDownLatch(3); 相當於 JUC裡的
    countDownLatch.trySetCount(count); //設定5個班
    countDownLatch.await();// 等待閉鎖都完成
    return "放假了 鎖門...";
  }

  //閉鎖 減少紀數器
  public String lockEd(String id) {
    RCountDownLatch countDownLatch = redissonClient.getCountDownLatch(LockDoorLock.keyName);

    countDownLatch.countDown();//記數器 -1

    return "班級號"+ id +"走了..";
  }


//*
// 數據一致性
// 1.雙寫模式 (不加鎖會髒讀)
// 2.失效模式 (不加鎖會髒讀) 可以加讀寫鎖
// *//
  public List<Dept> redissonLockList(String checkKeyName , String orderId) {

    //1.鎖的名字 鎖的粒度 越細越快
    //鎖的粒度 具體緩存是某個數據 11-號訂單: orderId-11-lock  orderId-12-lock

    RLock lock = redissonClient.getLock(RedissonLockList.keyName+orderId);

    lock.lock();
    List<Dept> list;
    //主要業務邏輯 **處理業務時間過長解決 : 設置長點的過期時間 在處理業務的時候無論成功或失敗都解鎖 或續期
    try {
      list = deptService.list(); //主要業務邏輯
      String jsonString = JSON.toJSONString(list);
      // 設置過期時間（加上隨機值）：緩存雪崩
      redisTemplate.opsForValue().set(checkKeyName, jsonString, RandomUtil.RandomExpireTime(), TimeUnit.SECONDS);
    }finally {

      lock.unlock();
    }
    return list;
  }


}
