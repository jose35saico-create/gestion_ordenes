package org.demo.orderservice.infrastructure.adapter;

import com.azure.storage.queue.QueueAsyncClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.orderservice.domain.model.Item;
import org.demo.orderservice.domain.model.Order;
import org.demo.orderservice.infrastructure.exception.QueueSendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

class OrderQueueAdapterTest {

    private QueueAsyncClient queueAsyncClient;
    private ObjectMapper objectMapper;
    private OrderQueueAdapter orderQueueAdapter;

    private Order order;

    @BeforeEach
    void setUp() {
        queueAsyncClient = Mockito.mock(QueueAsyncClient.class);
        objectMapper = Mockito.mock(ObjectMapper.class);
        orderQueueAdapter = new OrderQueueAdapter(queueAsyncClient, objectMapper);

        order = new Order("ORD123", "CUS001",
            List.of(new Item("PROD01", 2)), BigDecimal.valueOf(100.0));
    }

    @Test
    void shouldSendOrderSuccessfully() throws JsonProcessingException {
        String json = "{\"orderId\":\"ORD123\"}";
        when(objectMapper.writeValueAsString(order)).thenReturn(json);
        when(queueAsyncClient.createIfNotExists()).thenReturn(Mono.empty());
        when(queueAsyncClient.sendMessage(json)).thenReturn(Mono.empty());

        StepVerifier.create(orderQueueAdapter.sendOrder(order))
            .verifyComplete();

        verify(queueAsyncClient, times(1)).createIfNotExists();
        verify(queueAsyncClient, times(1)).sendMessage(json);
    }

    @Test
    void shouldThrowExceptionWhenSerializationFails() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(order))
            .thenThrow(new JsonProcessingException("fail") {});

        StepVerifier.create(orderQueueAdapter.sendOrder(order))
            .expectErrorMatches(throwable -> throwable instanceof QueueSendException &&
                throwable.getMessage().contains("Error al serializar la orden"))
            .verify();

        verify(queueAsyncClient, never()).sendMessage(anyString());
    }

    @Test
    void shouldThrowExceptionWhenSendFails() throws JsonProcessingException {
        String json = "{\"orderId\":\"ORD123\"}";
        when(objectMapper.writeValueAsString(order)).thenReturn(json);
        when(queueAsyncClient.createIfNotExists()).thenReturn(Mono.empty());
        when(queueAsyncClient.sendMessage(json)).thenReturn(Mono.error(new RuntimeException("Queue down")));

        StepVerifier.create(orderQueueAdapter.sendOrder(order))
            .expectErrorMatches(throwable -> throwable instanceof QueueSendException)
            .verify();
    }
}
