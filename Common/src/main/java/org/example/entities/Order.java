package org.example.entities;

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
public class Order {

  private Long id;

  private BigDecimal price;

  private Integer type;

  private Integer status;

  private Date update_time;

  private Date create_time;



}
