package org.example.enums;

public enum OrderStatusEnum {

  Fail("Fail",-1),
  Success("Success",1),
  CreateIng("CreateIng",2),
  PayIng("PayIng",3);



  public final String name;

  public final int code;

  OrderStatusEnum(String name, int code){

    this.name = name;
    this.code = code;

  }



}
