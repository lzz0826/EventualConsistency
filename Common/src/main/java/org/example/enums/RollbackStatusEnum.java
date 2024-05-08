package org.example.enums;

public enum RollbackStatusEnum {
    NotRollback(0),
    IsRollback(-1)
    ;

    public final int code;

    RollbackStatusEnum( int code){
        this.code = code;
    }


}
