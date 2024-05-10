package org.example.entities.middle;

import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)//鏈式風格訪問
@Builder
public class OrderStockMiddle {

  private Long id;

  private Long order_id;

  private Long stock_id;

  private Integer deducted_quantity;

  //定單狀態 Fail = -1, Success=1  ,CreateIng = 0
  private Integer status;

  private Date update_time;

  private Date create_time;



}
