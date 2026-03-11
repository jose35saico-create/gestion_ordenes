package org.demo.orderservice.application.service;

import org.demo.orderservice.application.port.out.OrderQueuePort;
import org.demo.orderservice.domain.model.Item;
import org.demo.orderservice.domain.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OrderServiceTest {

    private OrderQueuePort orderQueuePort;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderQueuePort = Mockito.mock(OrderQueuePort.class);
        orderService = new OrderService(orderQueuePort);
    }

    @Test
    void shouldSendOrderToQueuePort() {
        Order order = new Order(
            "ORD123",
            "CUS001",
            List.of(new Item("PROD01", 2)),
            BigDecimal.valueOf(100.0)
        );

        orderService.sendOrder(order);

        verify(orderQueuePort, times(1)).sendOrder(order);
    }
}
