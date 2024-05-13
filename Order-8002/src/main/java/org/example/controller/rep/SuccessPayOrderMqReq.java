package org.example.controller.rep;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessPayOrderMqReq {

    @NotNull(message = "product_quantity不能为空")
    public Long orderId;


}
