package org.demo.paymentprocessor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransaction {
    private String id;
    private String orderId;
    private BigDecimal amount;
    private Instant timestamp;
}
