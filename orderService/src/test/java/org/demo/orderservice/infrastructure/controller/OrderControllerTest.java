package org.demo.orderservice.infrastructure.controller;

import org.demo.orderservice.application.port.in.PlaceOrderUseCase;
import org.demo.orderservice.infrastructure.controller.dto.request.ItemRequest;
import org.demo.orderservice.infrastructure.controller.dto.request.OrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;

@WebFluxTest(controllers = OrderController.class)
class OrderControllerTest {

    @MockitoBean
    private PlaceOrderUseCase placeOrderUseCase;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldCreateOrderSuccessfully() {
        OrderRequest request = new OrderRequest(
            "ORD123",
            "CUS001",
            List.of(new ItemRequest("PROD01", 2)),
            BigDecimal.valueOf(150.0)
        );

        webTestClient.post()
            .uri("/api/orders")
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(String.class)
            .isEqualTo("Order placed successfully");
    }

    @Test
    void shouldFailWhenOrderIdIsBlank() {
        OrderRequest request = new OrderRequest(
            "",
            "CUS001",
            List.of(new ItemRequest("PROD01", 2)),
            BigDecimal.valueOf(150.0)
        );

        webTestClient.post()
            .uri("/api/orders")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shouldFailWhenItemsIsEmpty() {
        OrderRequest request = new OrderRequest(
            "ORD123",
            "CUS001",
            List.of(),
            BigDecimal.valueOf(150.0)
        );

        webTestClient.post()
            .uri("/api/orders")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shouldFailWhenTotalAmountIsInvalid() {
        OrderRequest request = new OrderRequest(
            "ORD123",
            "CUS001",
            List.of(new ItemRequest("PROD01", 2)),
            BigDecimal.ZERO
        );

        webTestClient.post()
            .uri("/api/orders")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest();
    }
}
