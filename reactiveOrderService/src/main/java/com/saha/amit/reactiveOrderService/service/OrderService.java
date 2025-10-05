package com.saha.amit.reactiveOrderService.service;


import com.saha.amit.reactiveOrderService.events.OrderEvent;
import com.saha.amit.reactiveOrderService.messanger.OrderEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;
@Slf4j
@Service
public class OrderService {

    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    public Mono<OrderEvent> placeOrder(String customerId, Double amount) {
        // Create order ID
        String orderId = UUID.randomUUID().toString();

        // Create order event
        OrderEvent event = OrderEvent.create(orderId, customerId, amount, "PLACED");

        // Publish to Kafka and return the event
        return orderEventPublisher.publish(event)
                .doOnNext(unused -> log.info("Order event published: {}", event))
                .doOnError(ex -> log.error("Error publishing event: {}", ex.getMessage()))
                .thenReturn(event);
    }
}
