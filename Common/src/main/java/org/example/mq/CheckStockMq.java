package org.example.mq;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)//鏈式風格訪問
@Builder
public class CheckStockMq {

    private Long stock_undo_log_id;

    private Long stock_id;

    private Long order_id;

}
