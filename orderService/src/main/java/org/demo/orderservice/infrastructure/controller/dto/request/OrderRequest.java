package org.demo.orderservice.infrastructure.controller.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @NotBlank
    private String orderId;

    @NotBlank
    private String customerId;

    @Size(min = 1)
    @Valid
    private List<ItemRequest> items;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal totalAmount;
}
