package org.example.controller.req;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeductedStockQuantityMqReq {

    @NotNull(message = "StockId不能为空")
    private Long stockId;

    @NotNull(message = "OrderId不能为空")
    private Long orderId;


    @NotNull(message = "quantity不能为空")
    private Integer quantity;
}
