package org.example.controller;


import java.util.List;
import javax.annotation.Resource;
import org.example.entities.Dept;
import org.example.enums.RedisKeyEnum;
import org.example.service.RedissonService;
import org.example.service.SpringCacheService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.example.service.DeptService;

@RestController
public class DeptController {

  @Resource
  private DeptService deptService;

  @Resource
  private RedissonService redissonService;

  @Resource
  private SpringCacheService springCacheService;

//  @GetMapping("/dept/discovery")
//  public Object discovery(){
//    List<String> services = client.getServices();
//    System.out.println("***"+services);
//
//    List<ServiceInstance> instances = client.getInstances("SPRINGCLOUDEX-DEPT");
//    for (ServiceInstance instance : instances) {
//      System.out.println(instance.getServiceId()+"\t"+instance.getHost()+"\t"+instance.getPort()+"\t"
//      +instance.getUri());
//    }
//    return this.client;
//
//  }

  @PostMapping("/dept/add")
  public boolean add(@RequestBody Dept dept){
    return deptService.add(dept);
  };

  @PostMapping("/dept/update")
  public int update(@RequestBody Dept dept){
    return deptService.update(dept);
  };

  @GetMapping("/dept/get/{id}")
  public Dept get(@PathVariable("id") Long id){
    return deptService.get(id);
  };


  @GetMapping("/dept/list")
  public List<Dept> list(){
    return deptService.list();
  };


  //----------------- Redis分布式鎖 --------------------

  //*
  // Redis分布式鎖 範例
  // *//

  //一般Redis分布式鎖
  @GetMapping("/dept/getCashByList")
  public List<Dept> getCashByList(){
    return deptService.getCashByList();
  };


  //----------------- Redisson分布式鎖 --------------------

  //Redisson分布式鎖
  @GetMapping("/testRedissonLock")
  public List<Dept> testRedissonLock(){
    return redissonService.redissonLock();
  };

  //*
  // Redisson讀寫鎖
  // *//

  //Redisson寫鎖
  @GetMapping("/testRedissonWriteLock")
  public String testRedissonWriteLock(){
    return redissonService.redissonWriteLock();
  };

  //Redisson讀鎖
  @GetMapping("/testRedissonReadLock")
  public String testRedissonReadLock(){
    return redissonService.redissonReadLock();
  };

  //*
  // Redisson 信號量 也作為分布式限流
  // 停車
  // 例: 有三個車位 停滿後 要有車離開 才能有新車近來停
  // *//
  @GetMapping("/parkCreat/{count}")
  public String parkCreat(@PathVariable("count")int count){
    return redissonService.parkCreat(count);
  };

  @GetMapping("/parkIn")
  public String parkIn() throws InterruptedException {
    return redissonService.parkIn();
  };

  @GetMapping("/parkOut")
  public String parkOut(){
    return redissonService.parkOut();
  };


  //*
  // Redisson、閉鎖
  // 放假 鎖門
  // 例 五個班級 要走完才能鎖門
  // *//

  @GetMapping("/lockDoor/{count}")
  public String lockDoor(@PathVariable int count) throws InterruptedException {

    return redissonService.lockDoor(count);
  };

  @GetMapping("/lockEd/{id}")
  public String lockEd(@PathVariable("id") String id){

    return redissonService.lockEd(id);
  };


  //----------------- SpringCache --------------------


  @GetMapping("/getSpringCacheList")
  public  List<Dept>  getSpringCacheList(){

    String orderId = "123";

    StringBuilder str = new StringBuilder(RedisKeyEnum.GetSpringCacheListCache.keyName);
    str.append(orderId);

    List<Dept> springCacheList = springCacheService.getSpringCacheList(str.toString());
    return springCacheList;
  };


  @PostMapping("/updateSpringCacheList")
  public int updateSpringCacheList(@RequestBody Dept dept){
    String orderId = "123";
    StringBuilder str = new StringBuilder(RedisKeyEnum.GetSpringCacheListCache.keyName);
    str.append(orderId);
    int update = springCacheService.updateSpringCacheList(dept,str.toString());
    return update;
  }







}
