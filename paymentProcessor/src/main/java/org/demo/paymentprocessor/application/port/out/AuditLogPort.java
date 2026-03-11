package org.demo.paymentprocessor.application.port.out;

import org.demo.paymentprocessor.domain.model.Order;
import reactor.core.publisher.Mono;

public interface AuditLogPort {
    Mono<Void> saveOrderAudit(Order order);
}
