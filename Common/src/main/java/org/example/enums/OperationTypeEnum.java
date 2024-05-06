package org.example.enums;

public enum OperationTypeEnum {

    //increase（增加庫存）、decrease（減少庫存)
    Increase("Increase"),

    Decrease("decrease"),
    ;

    public final String name;


OperationTypeEnum(String name){
    this.name = name;
}





}
