# EventualConsistency 緩存最終一致性:

![image](https://github.com/lzz0826/EventualConsistency/blob/main/img/003.png)

## 對於分散式事務，常用的解決方案根據一致性的程度可以進行如下劃分：

  * 強一致性(2PC、3PC)：資料庫層面的實現，透過鎖定資源，犧牲可用性，確保資料的強一致性，效率相對比較低。
  * 弱一致性(TCC)：業務層面的實現，透過預留或鎖定部分資源，最後透過確認或取消作業完成事務的處理。 例如A向B轉款500元，A帳號會凍結500元，其他操作正常，B接收轉款時，也不能直接入賬，而是將500元放到預留空間，只有經過確認之後，A才正式 扣錢，B才正式入帳； 若取消把A的500塊解凍，B也不會入帳。
  * 最終一致性(本地訊息表)：不管經過多少個服務節點，最終資料一致就行。 例如下單成功之後，需要庫存服務扣減庫存，如果庫存扣減失敗，不管是重試，還是最後人工處理，最後確保訂單和庫存數據能對上就行；為保證用戶體驗，及時通過中間狀態的 形式回饋給用戶，例如常見的出票中、資料處理中等。

<br />

## CAP:
  * C: 一致性（Consistency）。 指任何一個讀取操作總是能讀到之前完成的寫入操作的結果。 也就是說在我們分散式環境中，多點的資料必須是一致的。 所有節點在同一時間要具有相同的資料。  <br />
  * A: 可用性（Availability）。 指快速的獲取數據，可以在確定時間內返回操作結果，並保證每個請求不管成功或失敗都有回應。  <br />
  * P: 分區容忍性（Partition tolerance）。 指當網路出現分區的情況（即係統中的一部分節點無法和其他節點進行通訊）分離的系統也能夠正常運作。 即：系統中任意資訊遺失不會影響系統正常運作  <br />
![image](https://github.com/lzz0826/EventualConsistency/blob/main/img/008.png)
  

### 在面對CAP問題時有以下幾種選擇:
  * CA：放棄分區容忍性，來成就可用性和一致性。 把所有和事務相關的內容都放在一台機器上，避免網路分區。 這樣就根本不存在網路分割區的問題，傳統的關聯式資料庫、集中式資料庫，例如：Mysql、SQL Server，都是這種原則，因此他們的擴充性非常差。
  * CP：犧牲可用性，來成就一致性和分區容忍性。 犧牲可用性意義就是：當出現網路分區的時候，我可以等數據一致後再去取數據，此時短時間內無法取到數據，失去可用性。
  * AP：放棄一致性，來成就可用性和分割容忍性。 可及時取得數據，但是數據可能是不一致的。 即使是不一致的，我也馬上要這個數據。

<br />


## redis - 快取雪崩、擊穿、穿透:

### 1. 快取雪崩 (Cache Avalanche, 緩存雪崩) 
在某個時刻所有的 cache同時發生過期或者redis 服務失效，導致大量的 request 直接打在資料庫上，當流量巨大時資料庫很可能會被打掛，此時DBA若重啟資料庫可能又會被新的一波流量再打掛，這樣的狀況就是快取雪崩。  <br />
* 解決方案:   <br />
因為是同個時間點，所有的cache key大規模失效，因此可以在設定 cache 時，給予每個cache key 隨機的過期時間，或者 不設定過期時間 。每個cache key 過期時間的設定，其核心理念在你想要更新資料的頻率。

###  2. 快取擊穿 (Hotspot Invalid, 緩存擊穿)
快取擊穿和快取雪崩相似，雪崩是大面積cache key時間過期，而快取擊穿則是某個熱門的 cache key過期。所以，當高併發集中在此熱門的key又快取失效過期時，流量就會直接打在資料庫上，這樣子的狀況就叫快取擊穿。  <br />
* 解決方案:   <br />
要避免快取擊穿其中一個方法是將熱點key設為不過期，另一個方法則是在application寫lock(互斥鎖)以確保共用資源在多執行緒下可以排隊拿取資源，不過此作法會造成系統吞吐量下降，並阻礙其他線程。

###  3. 快取穿透 (Cache Penetration, 緩存穿透)
快取穿透是指client request 的資料並不存在於 cache 中並且也不存在於資料庫中，因此每次的請求就會直接穿過cache並打在資料庫中。同樣，若這樣類行的請求量一多，也是會將資料庫打掛。
* 解決方案:   <br />
因為是查找不存在的資料，因此可以在application中過濾非法請求，也就是當client 請求id = -1時，直接將請求做例外處理，不要讓他打在資料庫上。另一種方式，則是將id=-1寫入cache中並回傳對應的處理，例如當id=-1則redis則回傳null。還有另一種方式則是使用 " 布隆過濾器 "(Bloom Filter)判斷請求的key是否存在於集合中，若存在則直接去redis拿取，若不在則直接回傳對應訊息。


## Redisson分布式鎖:
讀寫所能保證讀到最新數據 修改期間 寫鎖:排他鎖(互斥鎖 獨享鎖)  讀鎖:共享鎖(相當於無鎖) <br />
寫鎖沒釋放讀就必須等待 <br />
讀 + 讀 相當於無鎖(共享鎖) 所有當前的讀鎖 都會同時加鎖成功 <br />
寫 + 讀 等待寫鎖釋放 <br />
寫 + 寫 阻塞鎖釋放 <br />
讀 + 寫 有讀鎖 寫也必須等待 <br />
只要有寫的存在 都必須等待 <br />
![image](https://github.com/lzz0826/EventualConsistency/blob/main/img/002.png)

 *信號量也作為分布式限流

 *使用Lua腳本確保原子性

數據一致性  <br />
1.雙寫模式 (不加鎖會髒讀)  <br />
![image](https://github.com/lzz0826/EventualConsistency/blob/main/img/004.png)
  
2.失效模式 (不加鎖會髒讀) 可以加讀寫鎖  <br />
![image](https://github.com/lzz0826/EventualConsistency/blob/main/img/005.png)
  



<br />

## Spring-Cache:

   一. 讀模式:
     緩存穿透: cache-null-values: true (解決)
     緩存擊穿: 加鎖 sync = true 加上本地鎖解決緩存擊穿 只有在查詢得時候有@Cacheable (解決)
     緩存雪崩: time-to-live: 3600000 (解決)   <br />
     <br />
   二. 寫模式: (緩存與數據一致)
     1.讀寫加鎖
     2.引入Canal 感知到MySQL的更新去更新數據
     3.讀多寫多 直接去數據庫就行   <br />
     <br />
   總結:
     常規數據(讀多寫少 即時性,一致性要求不高的數據)完全可以使用Spring-Cache
     寫模式(只要緩存的數據有過期時間就足夠了)
     特殊數據:特殊設計   <br />
     <br />
  
   原理: CacheManger(RedisCacheManager) -> Cache(RedisCache) -> Cache負責緩存的讀寫   <br />
   
   配置為Redis:
   spring:
    cache:
     type: redis
  
   @EnableCaching

<br />

# Seata 分布式事务:
  * AT（原子性事务）模式：
AT模式是Seata的默认模式，也是最常用的模式之一。
在AT模式中，Seata使用类似于本地事务的方式来管理分布式事务。当服务发起一个分布式事务时，Seata会自动将其转换为全局事务，并在需要的时候协调各个参与者提交或回滚事务。
AT模式使用的是数据库的undo log来实现事务的回滚。
  * TCC（Try-Confirm-Cancel）模式：
TCC模式是一种更加细粒度的分布式事务管理模式。
在TCC模式中，业务操作被分为三个阶段：尝试（Try）、确认（Confirm）和取消（Cancel）。
Seata通过调用各个参与者的Try、Confirm和Cancel方法来管理事务的执行和回滚。
  * SAGA模式：
SAGA模式是一种逐步补偿的分布式事务管理模式。
在SAGA模式中，分布式事务被分解为一系列可逆的局部事务，每个局部事务都有其自己的补偿操作。
Seata通过事务协调器来管理SAGA模式下的事务执行和补偿。
  * XA模式：
XA模式是一种经典的分布式事务管理模式，它基于X/Open XA协议。
在XA模式中，所有的数据库资源管理器（RM）都遵循相同的协议来协调全局事务。
Seata通过协调器来管理XA模式下的全局事务。


