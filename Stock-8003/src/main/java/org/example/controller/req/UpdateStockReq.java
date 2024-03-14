package org.example.controller.req;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStockReq {

  @NotNull(message = "id不能为空")
  private Long id;

  private Long product_id;

  private String product_name;

  @Min(0)
  private BigDecimal price;

  private Integer type;

  private Integer status;


}


