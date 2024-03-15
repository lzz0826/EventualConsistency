package org.example.controller.req;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeductedStockQuantityReq {

  @NotNull(message = "id不能为空")
  private Long id;

  @NotNull(message = "quantity不能为空")
  private Integer quantity;


}


