package org.demo.orderservice.infrastructure.adapter;

import com.azure.storage.queue.QueueAsyncClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.orderservice.application.port.out.OrderQueuePort;
import org.demo.orderservice.domain.model.Order;
import org.demo.orderservice.infrastructure.exception.QueueSendException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderQueueAdapter implements OrderQueuePort {

    private final QueueAsyncClient queueAsyncClient;
    private final ObjectMapper objectMapper;

    @Value("${azure.queue.timeout-seconds:5}")
    private int timeoutSeconds;

    @Value("${azure.queue.retry-max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${azure.queue.retry-backoff-seconds:2}")
    private int retryBackoffSeconds;

    @Override
    public Mono<Void> sendOrder(Order order) {
        String orderJson;
        try {
            orderJson = objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order: orderId={}", order.getOrderId());
            return Mono.error(new QueueSendException("Error al serializar la orden", e));
        }

        return queueAsyncClient.createIfNotExists()
            .then(queueAsyncClient.sendMessage(orderJson))
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .retryWhen(Retry.backoff(retryMaxAttempts, Duration.ofSeconds(retryBackoffSeconds))
                .doBeforeRetry(signal -> log.warn(
                    "Retrying sendMessage to Azure Queue (attempt {}): orderId={}",
                    signal.totalRetries() + 1, order.getOrderId())))
            .doOnSuccess(result -> log.info(
                "Order placed in Azure Queue: queueName={}, orderId={}",
                queueAsyncClient.getQueueName(), order.getOrderId()))
            .then()
            .onErrorMap(e -> {
                log.error("Failed to send order to queue after retries: orderId={}, queueName={}",
                    order.getOrderId(), queueAsyncClient.getQueueName(), e);
                return new QueueSendException("Error al enviar orden", e);
            });
    }
}