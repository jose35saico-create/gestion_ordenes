package org.demo.paymentprocessor.infrastructure.adapter.outbound;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.paymentprocessor.application.port.out.AuditLogPort;
import org.demo.paymentprocessor.domain.model.Order;
import org.demo.paymentprocessor.infrastructure.exception.AuditLogException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogAdapter implements AuditLogPort {

    private final BlobContainerClient blobContainerClient;
    private final ObjectMapper objectMapper;

    @Value("${azure.blob.timeout-seconds:5}")
    private int timeoutSeconds;

    @Value("${azure.blob.retry-max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${azure.blob.retry-backoff-seconds:2}")
    private int retryBackoffSeconds;

    @Override
    public Mono<Void> saveOrderAudit(Order order) {
        return Mono.fromRunnable(() -> {
            try {
                blobContainerClient.createIfNotExists();
                String orderItem = objectMapper.writeValueAsString(order);
                BlobClient blobClient = blobContainerClient.getBlobClient(order.getOrderId() + ".json");
                blobClient.upload(BinaryData.fromString(orderItem), true);
            } catch (JsonProcessingException e) {
                throw new AuditLogException("Error serializando la orden para auditoria", e);
            } catch (Exception e) {
                throw new AuditLogException("Error al guardar la auditoria de la orden " + order.getOrderId(), e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .timeout(Duration.ofSeconds(timeoutSeconds))
        .retryWhen(Retry.backoff(retryMaxAttempts, Duration.ofSeconds(retryBackoffSeconds))
            .doBeforeRetry(signal -> log.warn(
                "Retrying Blob Storage saveOrderAudit for orderId={} (attempt {})",
                order.getOrderId(), signal.totalRetries() + 1)))
        .then();
    }
}
