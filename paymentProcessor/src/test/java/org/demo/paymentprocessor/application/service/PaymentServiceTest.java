package org.demo.paymentprocessor.application.service;

import org.demo.paymentprocessor.application.port.out.AuditLogPort;
import org.demo.paymentprocessor.application.port.out.PaymentRepositoryPort;
import org.demo.paymentprocessor.domain.model.Item;
import org.demo.paymentprocessor.domain.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private PaymentRepositoryPort paymentRepository;
    private AuditLogPort auditLog;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = Mockito.mock(PaymentRepositoryPort.class);
        auditLog = Mockito.mock(AuditLogPort.class);

        paymentService = new PaymentService(paymentRepository, auditLog);
    }

    @Test
    void shouldProcessOrderSuccessfully() {
        Order order = new Order(
            "order-123",
            "customer-1",
            List.of(new Item("item-1", 2)),
            BigDecimal.valueOf(100.0)
        );

        when(paymentRepository.saveTransaction(Mockito.any()))
            .thenReturn(Mono.empty());

        when(auditLog.saveOrderAudit(order))
            .thenReturn(Mono.empty());

        Mono<Void> result = paymentService.processOrder(order);

        StepVerifier.create(result)
            .verifyComplete();

        verify(paymentRepository).saveTransaction(Mockito.any());
        verify(auditLog).saveOrderAudit(order);
    }

    @Test
    void shouldHandleErrorGracefully() {
        Order order = new Order(
            "order-123",
            "customer-1",
            List.of(new Item("item-1", 2)),
            BigDecimal.valueOf(100.0)
        );

        when(paymentRepository.saveTransaction(Mockito.any()))
            .thenReturn(Mono.error(new RuntimeException("DB error")));

        Mono<Void> result = paymentService.processOrder(order);

        StepVerifier.create(result)
            .verifyComplete();

        verify(auditLog, Mockito.never()).saveOrderAudit(order);
    }
}
