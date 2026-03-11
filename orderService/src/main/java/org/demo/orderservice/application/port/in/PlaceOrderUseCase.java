package org.demo.orderservice.application.port.in;

import org.demo.orderservice.domain.model.Order;
import reactor.core.publisher.Mono;

public interface PlaceOrderUseCase {
    Mono<Void> sendOrder(Order order);
}
