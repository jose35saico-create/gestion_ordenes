package org.demo.orderservice.application.port.out;

import org.demo.orderservice.domain.model.Order;
import reactor.core.publisher.Mono;

public interface OrderQueuePort {
    Mono<Void> sendOrder(Order order);
}