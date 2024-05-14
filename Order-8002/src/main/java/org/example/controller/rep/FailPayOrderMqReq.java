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
public class FailPayOrderMqReq {

    @NotNull(message = "orderId不能为空")
    public Long orderId;


}
