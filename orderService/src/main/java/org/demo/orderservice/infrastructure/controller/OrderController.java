package org.demo.orderservice.infrastructure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.orderservice.application.port.in.PlaceOrderUseCase;
import org.demo.orderservice.domain.model.Order;
import org.demo.orderservice.infrastructure.controller.dto.request.OrderRequest;
import org.demo.orderservice.infrastructure.mapper.OrderMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createOrder(@RequestBody @Valid OrderRequest orderRequest) {
        Order order = OrderMapper.toDomain(orderRequest);
        return placeOrderUseCase.sendOrder(order)
            .then(Mono.just("Order placed successfully"));
    }
}
