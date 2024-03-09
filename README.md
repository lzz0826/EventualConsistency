# EventualConsistency 緩存最終一致性:

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

<br />
