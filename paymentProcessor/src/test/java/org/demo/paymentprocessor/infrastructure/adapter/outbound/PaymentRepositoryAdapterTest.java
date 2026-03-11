package org.demo.paymentprocessor.infrastructure.adapter.outbound;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import org.demo.paymentprocessor.domain.model.PaymentTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

class PaymentRepositoryAdapterTest {

    private CosmosContainer container;
    private PaymentRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        container = Mockito.mock(CosmosContainer.class);
        adapter = new PaymentRepositoryAdapter(container);
    }

    @Test
    void shouldSaveTransactionSuccessfully() {
        PaymentTransaction transaction = new PaymentTransaction("tx-123", "order-456", BigDecimal.valueOf(100.0), Instant.now());
        Mockito.when(container.createItem(transaction)).thenReturn(Mockito.mock(CosmosItemResponse.class));

        Mono<Void> result = adapter.saveTransaction(transaction);

        StepVerifier.create(result)
            .verifyComplete();

        Mockito.verify(container).createItem(transaction);
    }

    @Test
    void shouldThrowExceptionWhenSaveFails() {
        PaymentTransaction tx = new PaymentTransaction("tx-123", "order-456", BigDecimal.valueOf(100.0), Instant.now());

        Mockito.doThrow(new RuntimeException("Error DB")).when(container).createItem(tx);

        Mono<Void> result = adapter.saveTransaction(tx);

        StepVerifier.create(result)
            .expectErrorMatches(throwable ->
                throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Error DB"))
            .verify();

        Mockito.verify(container).createItem(tx);
    }
}
