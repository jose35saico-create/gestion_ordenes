package org.demo.paymentprocessor.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.paymentprocessor.application.port.in.ProcessPaymentUseCase;
import org.demo.paymentprocessor.application.port.out.AuditLogPort;
import org.demo.paymentprocessor.application.port.out.PaymentRepositoryPort;
import org.demo.paymentprocessor.domain.model.Order;
import org.demo.paymentprocessor.domain.model.PaymentTransaction;
import org.demo.paymentprocessor.infrastructure.exception.PaymentProcessingException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements ProcessPaymentUseCase {

    private final PaymentRepositoryPort paymentRepository;
    private final AuditLogPort auditLog;

    @Override
    public Mono<Void> processOrder(Order order) {
        PaymentTransaction paymentTransaction = new PaymentTransaction(
            UUID.randomUUID().toString(),
            order.getOrderId(),
            order.getTotalAmount(),
            Instant.now()
        );

        return paymentRepository.saveTransaction(paymentTransaction)
            .then(Mono.defer(() -> auditLog.saveOrderAudit(order)))
            .onErrorResume(e -> {
                log.error("Error procesando orden {}: {}", order.getOrderId(), e.getMessage());
                return Mono.empty();
            });
    }
}
