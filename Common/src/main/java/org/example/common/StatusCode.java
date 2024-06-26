package org.example.common;

public enum StatusCode {

 /**
 * 系統 0
 */
  Success(0,"成功"),

  SystemError(-1,"失敗"),

  MissingParameter(10,"缺少必要參數"),

  ErrorParameter(11,"參數錯誤"),

  AddFail(12,"新增失敗"),

  NeedPage(13,"需要頁碼"),

  NeedPageSize(14,"需要頁碼大小"),

  NotAllowedNullStr(15,"不允許空的字串"),

  BindExceptionError(16,"數據綁定錯誤,參數型別錯誤"),

  JSONSerializationFail(17,"JSON序列化失敗"),




  /**
   * OkHttp Client 100
   */
  OkHttpGetFail(100,"Client GET請求失敗"),

  OkHttpGetPost(101,"Client Post請求失敗"),


  /**
  * Order 2000
  */

  NotFoundOrder(2000,"找不到訂單"),

  OrderTypeError(2001,"訂單類型異常"),

  OrderStatusError(2002,"訂單狀態異常"),

  CreateOrderFail(2003,"新增訂單失敗"),

  CallBackProcessing(2004,"回調處理中"),
  DeductedStockQuantityFail(2005,"扣庫存失敗"),

  AddOrderFail(2006,"添加DB AddOrder失敗"),

  AddOrderStockMiddleFail(2006,"添加DB AddOrderStockMiddle失敗"),

  OrderServerError(2007,"Order服務異常"),

  NotFoundUpdateOrder(2008,"找不到需要更新的訂單"),



 /**
   * Stock 3000
   */
  UpdateStockFail(3000,"更新庫存失敗"),

  AddStockOnDoLogFail(3001,"添加庫存操作失敗"),

  StockServerError(3001,"Stock服務異常"),


  NoStock(3002,"沒有庫存"),




  ;

  public final int code;

  public final String msg;


  StatusCode(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public static StatusCode getByCode(int code) {
    for (StatusCode e : StatusCode.values()) {
      if (e.code == code) {
        return e;
      }
    }

    return null;
  }




}
