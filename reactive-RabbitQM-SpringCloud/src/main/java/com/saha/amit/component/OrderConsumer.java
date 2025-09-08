package com.saha.amit.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saha.amit.dto.OrderDto;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.AcknowledgableDelivery;
import reactor.rabbitmq.Receiver;

import java.util.UUID;

@Component
public class OrderConsumer {

    private final Receiver receiver;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;
    private final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    public OrderConsumer(Receiver receiver,
                         ObjectMapper objectMapper,
                         EventPublisher eventPublisher) {
        this.receiver = receiver;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void subscribe() {
        receiver.consumeManualAck("order-service-queue")
                .flatMap(this::processDelivery)   // return Mono<Void> per message
                .doOnError(err -> logger.error("Unexpected error in consumer stream", err))
                .subscribe(); // start consuming
    }

    private Mono<Void> processDelivery(AcknowledgableDelivery delivery) {
        try {
            OrderDto order = objectMapper.readValue(delivery.getBody(), OrderDto.class);
            // assign an id if not present
            if (order.getOrderId() == null || order.getOrderId().isBlank()) {
                order.setOrderId(UUID.randomUUID().toString());
            }
            logger.info("Deserialized Order: {}", order);

            // validation: amount must be > 0
            if (order.getAmount() <= 0) {
                logger.warn("Invalid order (amount <= 0). Sending to DLQ: {}", order);
                // nack without requeue -> goes to DLQ if queue has x-dead-letter-exchange
                try {
                    delivery.nack(false);
                } catch (Exception e) {
                    logger.error("Failed to nack invalid message", e);
                }
                return Mono.empty();
            }

            // valid: publish payment.created and ack only on success
            return eventPublisher.publishEvent("payment.created", order)
                    .doOnSuccess(v -> {
                        try {
                            delivery.ack();
                            logger.info("Message acked and payment.created published for order {}", order.getOrderId());
                        } catch (Exception e) {
                            // if ack fails for some reason, we log and attempt to nack to avoid losing message
                            logger.error("Failed to ack message after successful publish. Nacking to DLQ", e);
                            try {
                                delivery.nack(false);
                            } catch (Exception ex) {
                                logger.error("Failed to nack after ack failure", ex);
                            }
                        }
                    })
                    .doOnError(err -> {
                        // publishing failed -> nack so message goes to DLQ
                        logger.error("Failed to publish payment.created for order {} — nacking to DLQ", order.getOrderId(), err);
                        try {
                            delivery.nack(false);
                        } catch (Exception e) {
                            logger.error("Failed to nack after publish error", e);
                        }
                    })
                    .onErrorResume(e -> Mono.empty()); // swallow downstream errors so stream continues

        } catch (Exception ex) {
            // deserialization or other immediate errors -> nack to DLQ
            logger.error("Deserialization/processing failed, sending to DLQ", ex);
            try {
                delivery.nack(false);
            } catch (Exception e) {
                logger.error("Failed to nack after deserialization error", e);
            }
            return Mono.empty();
        }
    }


    /*In case we don't want manual ack/nack handling, we can use autoAck as shown below.
    This is simpler but less robust as any processing error after auto-ack will lead to message loss.
    @PostConstruct
    public void subscribe() {
        // Listen to queue (must be bound to exchange in RabbitMQ)
        receiver.consumeAutoAck("order-service-queue")
                .map(delivery -> {
                    try {
                        OrderDto orderDto = objectMapper.readValue(delivery.getBody(), OrderDto.class);
                        orderDto.setOrderId(UUID.randomUUID().toString());
                        logger.info("Deserialized Order: {}", orderDto);
                        return orderDto;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize message", e);
                    }
                })
                .subscribe(order -> {
                    System.out.println("✅ Received Order: " + order.getOrderId()
                            + " for customer: " + order.getCustomerId());
                    // TODO: process order, e.g., save to DB, emit payment.created, etc.
                });
    }*/
}
