package org.demo.paymentprocessor.application.port.out;

import org.demo.paymentprocessor.domain.model.PaymentTransaction;
import reactor.core.publisher.Mono;

public interface PaymentRepositoryPort {
    Mono<Void> saveTransaction(PaymentTransaction transaction);
}
