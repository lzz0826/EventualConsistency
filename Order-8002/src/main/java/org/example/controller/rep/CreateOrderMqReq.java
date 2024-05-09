package org.example.controller.rep;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderMqReq {
    @NotNull(message = "product_quantity不能为空")
    Map<String,Integer> product_quantity;

}
