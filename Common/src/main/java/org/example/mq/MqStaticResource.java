package org.example.mq;

public class MqStaticResource {

    /**
     *  Order
     */
    //延遲隊列 (死信后会交给 orderReleaseOrderQueue)
    public static final String Order_Delay_Queue_Name = "order.delay.queue" ;

    //訂單超時驗證隊列
    public static final String Order_Release_OrderQueue_Name = "order.release.order.queue" ;

    //事件交换机
    public static final String Order_Event_Exchange = "order-event-exchange" ;

    //創建訂單Key
    public static final String Order_Create_Order_Key = "order.create.order" ;

    //發布訂單Key
    public static final String Order_Release_Order_Key = "order.release.order" ;

    //訂單超時回滾Key
    public static final String Order_Release_Other_Key = "order.release.other";

    //訂單付款成功Key
    public static final String Order_Finish_Key = "order.finish";


    /**
     *  Order Notify
     */
    //Order服務主動通知(回滾)專用Key(其他服務的回滾 Queue 需要綁定此key)
    public static final String Order_Release_Other_Notify_Key = "order.release.other.#";

    //Order服務主動通知(付款成功)專用Key(其他服務付款成功後續 Queue 需要綁定此key)
    public static final String Order_Finish_Notify_Key = "order.finish.#";




    /**
     *  Stock
     */
    public static final String Stock_Delay_Queue_Name = "stock.delay.queue";
    public static final String Stock_Release_Stock_Queue_Name = "stock.release.stock.queue";
    public static final String Stock_Event_Exchange = "stock-event-exchange";
    public static final String Stock_Locked_Key = "stock.locked";
    public static final String Stock_Release_Key = "stock.release.#";













}
