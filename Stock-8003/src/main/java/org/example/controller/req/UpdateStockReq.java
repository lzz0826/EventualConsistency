package org.example.controller.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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


