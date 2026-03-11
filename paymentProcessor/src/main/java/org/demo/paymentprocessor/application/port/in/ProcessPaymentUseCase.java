package org.demo.paymentprocessor.application.port.in;

import org.demo.paymentprocessor.domain.model.Order;
import reactor.core.publisher.Mono;

public interface ProcessPaymentUseCase {
    Mono<Void> processOrder(Order order);
}
