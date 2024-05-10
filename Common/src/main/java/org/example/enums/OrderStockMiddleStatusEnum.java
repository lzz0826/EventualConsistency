package org.example.enums;

public enum OrderStockMiddleStatusEnum {

    Fail("Fail",-1),
    CreateIng("CreateIng",0),

    Success("Success",1),
    PayIng("PayIng",2);


    public final String name;

    public final int code;

    OrderStockMiddleStatusEnum(String name, int code){

        this.name = name;
        this.code = code;

    }



}
