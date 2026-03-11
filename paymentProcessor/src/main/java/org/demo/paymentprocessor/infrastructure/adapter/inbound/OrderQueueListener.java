package org.demo.paymentprocessor.infrastructure.adapter.inbound;

import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.paymentprocessor.application.port.in.ProcessPaymentUseCase;
import org.demo.paymentprocessor.domain.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderQueueListener {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final ObjectMapper objectMapper;
    private final QueueAsyncClient queueAsyncClient;

    @Value("${azure.queue.timeout-seconds:5}")
    private int timeoutSeconds;

    @Value("${azure.queue.retry-max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${azure.queue.retry-backoff-seconds:2}")
    private int retryBackoffSeconds;

    @PostConstruct
    public void startListening() {
        queueAsyncClient.createIfNotExists()
            .thenMany(Flux.interval(Duration.ofSeconds(3)))
            .flatMap(tick -> queueAsyncClient.receiveMessages(5)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .retryWhen(Retry.backoff(retryMaxAttempts, Duration.ofSeconds(retryBackoffSeconds))
                    .doBeforeRetry(s -> log.warn("Retrying receiveMessages from Azure Queue (attempt {})", s.totalRetries() + 1))))
            .flatMap(this::handleMessage)
            .onErrorContinue((err, obj) -> log.error("Error processing message {}: {}", obj, err.getMessage()))
            .subscribe();
    }

    public Mono<Void> handleMessage(QueueMessageItem message) {
        try {
            Order order = objectMapper.readValue(message.getBody().toString(), Order.class);
            log.info("message: {}", message.getBody().toString());
            log.info("received order: {}", order.getOrderId());

            return processPaymentUseCase.processOrder(order)
                .doOnSuccess(v -> log.info("Processed order: {}", order.getOrderId()))
                .doOnError(err -> log.error("Error processing order {}: {}", order.getOrderId(), err.getMessage()))
                .then(Mono.defer(() ->
                    queueAsyncClient.deleteMessage(message.getMessageId(), message.getPopReceipt())
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .retryWhen(Retry.backoff(retryMaxAttempts, Duration.ofSeconds(retryBackoffSeconds))
                            .doBeforeRetry(s -> log.warn("Retrying deleteMessage for orderId={} (attempt {})",
                                order.getOrderId(), s.totalRetries() + 1)))
                        .doOnSuccess(v -> log.info("Deleted message from queue for order {}", order.getOrderId()))
                        .doOnError(err -> log.error("Failed to delete message for order {}: {}", order.getOrderId(), err.getMessage()))
                ));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse order message: {}", e.getMessage());
            return Mono.empty();
        }
    }
}
