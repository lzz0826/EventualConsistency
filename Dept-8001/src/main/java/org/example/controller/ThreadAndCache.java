package org.example.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Cashes{
  public static final Map<String,String> map2 = new ConcurrentHashMap<>();
  public void set(String k ,String v){
    map2.put(k,v);
  }

  public String get(String s){
    return map2.get(s);
  }

}

public class ThreadAndCache {

  public static void main(String[] args) {

    Lock lock = new ReentrantLock();

    lock.lock();

    try {

    }finally {
      lock.lock();
    }

    //線程池
    ExecutorService executorService = Executors.newFixedThreadPool(3);
    Cashes cashes = new Cashes();
    cashes.set("111","222");

    for(int i = 0 ; i<10 ; i++){
      executorService.execute(()->{
        String s = cashes.get("111");
        System.out.println(s);
      });

    }

    executorService.shutdown();
    //線程
    Thread thread1 = new Thread(()->{
      String s = cashes.get("111");
      System.out.println(s+Thread.currentThread().getName());
    },"t1");

    Thread thread2 = new Thread(()->{
      String s = cashes.get("111");
      System.out.println(s+Thread.currentThread().getName());
    },"t2");

    thread1.start();
    thread2.start();
  }

}
