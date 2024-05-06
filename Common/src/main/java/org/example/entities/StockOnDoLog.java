package org.example.entities;


import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)//鏈式風格訪問
@Builder
public class StockOnDoLog {
    private Long id;

    private Long stock_id;

    private Long order_id;

    //increase（增加庫存）、decrease（減少庫存)'
    private String operation_type;

    private Integer quantity;

    private Date operation_time;

    private String description;

    //'操作狀態：1-成功、-1-失敗、0-等待等'
    private Integer status;

    //'回滾狀態：0-未回滾、1-已回滾',
    private Integer rollback_status;

    private Date rollback_time;

    private Date update_time;

    private Date create_time;

}
