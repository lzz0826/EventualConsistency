package org.example.enums;

public enum RedisKeyEnum {


  //Cache
  GetCashByListCache("GetCashByListCache"),

  GetSpringCacheListCache("GetSpringCacheListCache"),

  //Lock
  GetCashByListLock("GetCashByListLock"),

  RedissonWriteLock("RedissonWriteLock"),

  Park("Park"),

  RedissonLockList("RedissonLockList"),

  LockDoorLock("LockDoor");


  public final String keyName;

  RedisKeyEnum(String keyName){
    this.keyName = keyName;
  }


}
