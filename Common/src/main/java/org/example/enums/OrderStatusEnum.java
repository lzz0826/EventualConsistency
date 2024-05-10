package org.example.enums;

public enum OrderStatusEnum {

  Fail("Fail",-1),
  CreateIng("CreateIng",0),

  Success("Success",1),
  PayIng("PayIng",2);



  public final String name;

  public final int code;

  OrderStatusEnum(String name, int code){

    this.name = name;
    this.code = code;

  }



}
