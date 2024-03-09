# EventualConsistency 緩存最終一致性:

<br />


### Redisson分布式鎖:
讀寫所能保證讀到最新數據 修改期間 寫鎖:排他鎖(互斥鎖 獨享鎖)  讀鎖:共享鎖(相當於無鎖) <br />
寫鎖沒釋放讀就必須等待 <br />
讀 + 讀 相當於無鎖(共享鎖) 所有當前的讀鎖 都會同時加鎖成功 <br />
寫 + 讀 等待寫鎖釋放 <br />
寫 + 寫 阻塞鎖釋放 <br />
讀 + 寫 有讀鎖 寫也必須等待 <br />
只要有寫的存在 都必須等待 <br />

 *信號量也作為分布式限流

 *使用Lua腳本確保原子性

數據一致性  <br />
1.雙寫模式 (不加鎖會髒讀)  <br />
2.失效模式 (不加鎖會髒讀) 可以加讀寫鎖  <br />

<br />

## Spring-Cache:

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
  * 配置為Redis:
  * spring:
  *  cache:
  *   type: redis
  *
  * @EnableCaching

<br />

