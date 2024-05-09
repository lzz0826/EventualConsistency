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
public class CreateOrderContent {

    @NotNull(message = "product_name不能为空")
    private String product_name;

    @NotNull(message = "quantity不能为空")
    private int quantity;
}
