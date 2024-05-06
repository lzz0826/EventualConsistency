package org.example.enums;

public enum StockOnDoLogEnum {
    //'操作狀態：1-成功、-1-失敗、0-等待等'
    Wait("wait",0),
    Success("success",1),

    Fail("fail",-1),
    ;

    public final String name;

    public final int code;

    StockOnDoLogEnum(String name,int code){
        this.name = name;
        this.code = code;
    }

}
