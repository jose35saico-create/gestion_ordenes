package org.demo.paymentprocessor.infrastructure.adapter.inbound;

import com.azure.core.util.BinaryData;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.paymentprocessor.application.port.in.ProcessPaymentUseCase;
import org.demo.paymentprocessor.domain.model.Item;
import org.demo.paymentprocessor.domain.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrderQueueListenerTest {

    private ProcessPaymentUseCase processPaymentUseCase;
    private ObjectMapper objectMapper;
    private QueueAsyncClient queueAsyncClient;
    private OrderQueueListener listener;

    @BeforeEach
    void setUp() {
        processPaymentUseCase = mock(ProcessPaymentUseCase.class);
        objectMapper = mock(ObjectMapper.class);
        queueAsyncClient = mock(QueueAsyncClient.class);

        listener = new OrderQueueListener(processPaymentUseCase, objectMapper, queueAsyncClient);
    }

    @Test
    void shouldProcessValidMessageAndDelete() throws JsonProcessingException {
        Order order = new Order("order-1", "customer-1", List.of(new Item("item-1", 2)), BigDecimal.valueOf(100));
        QueueMessageItem message = mock(QueueMessageItem.class);

        when(message.getBody()).thenReturn(BinaryData.fromString("{\"orderId\":\"order-1\"}"));
        when(message.getMessageId()).thenReturn("msg-1");
        when(message.getPopReceipt()).thenReturn("pop-1");

        when(objectMapper.readValue(anyString(), Mockito.eq(Order.class))).thenReturn(order);
        when(processPaymentUseCase.processOrder(order)).thenReturn(Mono.empty());
        when(queueAsyncClient.deleteMessage("msg-1", "pop-1")).thenReturn(Mono.empty());

        Mono<Void> result = listener.handleMessage(message);

        StepVerifier.create(result).verifyComplete();
        verify(processPaymentUseCase).processOrder(order);
        verify(queueAsyncClient).deleteMessage("msg-1", "pop-1");
    }

    @Test
    void shouldHandleInvalidJsonGracefully() throws Exception {
        QueueMessageItem message = mock(QueueMessageItem.class);
        when(message.getBody()).thenReturn(BinaryData.fromString("{invalid json}"));
        when(objectMapper.readValue(anyString(), eq(Order.class))).thenThrow(new JsonProcessingException("oops") {});

        Mono<Void> result = listener.handleMessage(message);

        StepVerifier.create(result).verifyComplete();
        verify(processPaymentUseCase, never()).processOrder(any());
        verify(queueAsyncClient, never()).deleteMessage(anyString(), anyString());
    }

    @Test
    void shouldNotDeleteMessageWhenProcessingFails() throws JsonProcessingException {
        QueueMessageItem message = mock(QueueMessageItem.class);
        Order order = new Order("order-1", "customer-1", List.of(new Item("item-1", 2)), BigDecimal.valueOf(100));

        when(message.getBody()).thenReturn(BinaryData.fromString("{\"orderId\":\"order-1\"}"));
        when(message.getMessageId()).thenReturn("msg-1");
        when(message.getPopReceipt()).thenReturn("pop-1");

        when(objectMapper.readValue(anyString(), eq(Order.class))).thenReturn(order);
        when(processPaymentUseCase.processOrder(order)).thenReturn(Mono.error(new RuntimeException("Payment failed")));

        Mono<Void> result = listener.handleMessage(message);

        StepVerifier.create(result).verifyErrorMessage("Payment failed");
        verify(queueAsyncClient, never()).deleteMessage(anyString(), anyString());
    }
}
