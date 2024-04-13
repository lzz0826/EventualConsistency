package org.example.service;

import jakarta.annotation.Resource;
import java.util.List;
import org.example.entities.Dept;
import org.example.service.impl.DeptServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SpringCacheService {
  /*
  *Spring-Cache的不足:
  * 1) 讀模式:
  *   緩存穿透: cache-null-values: true (解決)
  *   緩存擊穿: 加鎖 sync = true 加上本地鎖解決緩存擊穿 只有在查詢得時候有@Cacheable (解決)
  *   緩存雪崩: time-to-live: 3600000 (解決)
  * 2) 寫模式: (緩存與數據一致)
  *   1.讀寫加鎖
  *   2.引入Canal 感知到MySQL的更新去更新數據
  *   3.讀多寫多 直接去數據庫就行
  * 總結:
  *   常規數據(讀多寫少 即時性,一致性要求不高的數據)完全可以使用Spring-Cache
  *   寫模式(只要緩存的數據有過期時間就足夠了)
  *   特殊數據:特殊設計
  *
  * 原理: CacheManger(RedisCacheManager) -> Cache(RedisCache) -> Cache負責緩存的讀寫
  *
  * @Cacheable: Triggers cache population. : 觸發將數據保存到緩存中的操作
  * @CacheEvict: Triggers cache eviction. : 觸發將數據重緩存刪除操作
  * @CachePut: Updates the cache without interfering with the method execution. : 不引響方法執行更新緩存
  * @Caching: Regroups multiple cache operations to be applied on a method. : 組合以上多個操作
  * @CacheConfig: Shares some common cache-related settings at class level. : 在類級別共享緩存的相同配置
  *
  * 配置為Redis:
  * spring:
  *  cache:
  *   type: redis
  *
  * @EnableCaching
  *
  * */

  @Resource
  private DeptServiceImpl deptService;

  /* @Cacheable
  * 默認行為 1.如果緩存有 方法不調用
  *        2.key默認生成 dept(緩存區域名)::SimpleKey []
  *        3.緩存的value的值 默認使用jdb序列畫機制 將序列化後的數據存到redis
  *        4.默認ttl時間 -1 (永不過期)
  *
  * 需要自訂: 1.指定生成的緩存key : key屬性指定 接受SpEL(可動態取值 接收表達式)
  *         2.指定緩存的數據存活時間 : 在配置文件中設定
  *         3.將數據保存為json格式 :
  *          原理:
  *          CacheAutoConfiguration -> RedisCacheConfiguration ->
  *          自動配置了  RedisCacheManager -> 初始話所有緩存 -> 每個緩存決定使用什麼配置
  *          -> 如果redisCacheConfiguration有就用已有的 沒有就用默認配置
  *          -> 想改緩存的配置 只需要給容器中放一個 RedisCacheConfigureation即可
  *          -> 就會應用到當前 RedisCacheManager管理的所有緩存分區中
  *
  * */

  //value: 設定分區名 可以多個(建議依照業務類分)
//  @Cacheable(value = {"dept","xxxx"},key = "'dept'")// 表示返回結果需要緩存 如果緩存有 方法不調用 沒有調用方法 最後將結果放入緩存
  //sync = true 加上本地鎖解決緩存擊穿 只有在查詢得時候有@Cacheable
  @Cacheable(value = {"dept"}, key = "#cacheKey",sync = true) // 使用參數帶入key 使用方法名:#root.method.name
  public List<Dept> getSpringCacheList(String cacheKey){
    List<Dept> list = deptService.list();
    return list;
  }




  //@CacheEvict:失效模式:
  //** @CacheEvict註解以下如果出現異常不會刪除存 以上會正常刪除
  //1.同時進行多種緩存操做 @Caching
//  @Caching(evict = {
//      @CacheEvict(value = "dept" , key = "#cacheKey"),
//      @CacheEvict(value = "dept2" , key = "#cacheKey2")
//  })
  //2.指定刪除某分區下所有數據 @CacheEvict(value = "dept" , allEntries = true)
//  @CacheEvict(value = "dept" , allEntries = true)
  //3.存儲同一類的數據 都可以指定成同一分區 分區名就是緩存的前綴(推薦操作)
  //  @CachePut()//雙寫模式 返回的東西在緩存再存一份
  @CacheEvict(value = "dept" , key = "#cacheKey")
  public int updateSpringCacheList(Dept dept,String cacheKey){
    int update = deptService.update(dept);
//    int ee = 10/0; ** CacheEvict註解以下如果出現異常不會刪除存 以上會正常刪除
    return update;
  }








}
