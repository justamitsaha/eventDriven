package com.saha.amit.reactiveOrderService.service;

import com.saha.amit.reactiveOrderService.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OutboxService outboxService;

    public Mono<OrderEvent> placeOrder(String customerId, Double amount) {
        if (amount == null || amount <= 0) {
            log.error("Invalid amount provided: {}", amount);
            return Mono.error(new IllegalArgumentException("Amount must be greater than zero"));
        }

        return outboxService.persistOrderAndOutbox(customerId, amount)
                .doOnSuccess(event -> log.info("Order {} queued for publishing via outbox", event.orderId()));
    }
}
