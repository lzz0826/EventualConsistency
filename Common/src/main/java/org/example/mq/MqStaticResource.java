package org.example.mq;

public class MqStaticResource {

    /**
     *  Order
     */
    //延遲隊列 (死信后会交给 orderReleaseOrderQueue)
    public static final String Order_Delay_Queue_Name = "order.delay.queue" ;

    //訂單發布隊列
    public static final String Order_Release_OrderQueue_Name = "order.release.order.queue" ;

    //事件交换机
    public static final String Order_Event_Exchange = "order-event-exchange" ;

    //發布訂單綁定
    public static final String Order_Release_Order_Key = "order.release.order" ;

    //通知回滾專用Key(其他服務的回滾Queue需要綁定此key)
    public static final String Order_Release_Other_Key = "order.release.other.#";



    /**
     *  Stock
     */
    public static final String Stock_Delay_Queue_Name = "stock.delay.queue";
    public static final String Stock_Release_Stock_Queue_Name = "stock.release.stock.queue";
    public static final String Stock_Event_Exchange = "stock-event-exchange";
    public static final String Stock_Locked_Key = "stock.locked";
    public static final String Stock_Release_Key = "stock.release.#";













}
