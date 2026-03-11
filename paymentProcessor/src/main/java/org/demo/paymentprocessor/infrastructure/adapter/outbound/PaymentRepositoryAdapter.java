package org.demo.paymentprocessor.infrastructure.adapter.outbound;

import com.azure.cosmos.CosmosContainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.paymentprocessor.application.port.out.PaymentRepositoryPort;
import org.demo.paymentprocessor.domain.model.PaymentTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final CosmosContainer container;

    @Value("${azure.cosmos.timeout-seconds:5}")
    private int timeoutSeconds;

    @Value("${azure.cosmos.retry-max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${azure.cosmos.retry-backoff-seconds:2}")
    private int retryBackoffSeconds;

    @Override
    public Mono<Void> saveTransaction(PaymentTransaction transaction) {
        return Mono.fromRunnable(() -> container.createItem(transaction))
            .subscribeOn(Schedulers.boundedElastic())
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .retryWhen(Retry.backoff(retryMaxAttempts, Duration.ofSeconds(retryBackoffSeconds))
                .doBeforeRetry(signal -> log.warn(
                    "Retrying Cosmos DB saveTransaction for transactionId={} (attempt {})",
                    transaction.getId(), signal.totalRetries() + 1)))
            .then();
    }
}
