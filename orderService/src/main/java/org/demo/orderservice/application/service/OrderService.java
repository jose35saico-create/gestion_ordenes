package org.demo.orderservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.orderservice.application.port.in.PlaceOrderUseCase;
import org.demo.orderservice.application.port.out.OrderQueuePort;
import org.demo.orderservice.domain.model.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements PlaceOrderUseCase {

    private final OrderQueuePort orderQueuePort;

    @Override
    public Mono<Void> sendOrder(Order order) {
        log.info("Processing new order: orderId={}, customerId={}, totalAmount={}",
            order.getOrderId(), order.getCustomerId(), order.getTotalAmount());

        return orderQueuePort.sendOrder(order)
            .doOnSuccess(unused -> log.info("Order successfully sent to queue: orderId={}", order.getOrderId()))
            .doOnError(e -> log.error("Failed to send order: orderId={}", order.getOrderId(), e));
    }
}
