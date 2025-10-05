package com.saha.amit.reactiveOrderService.messanger;

import com.saha.amit.reactiveOrderService.events.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@Service
public class OrderEventPublisher {

    private final  KafkaSender<String, OrderEvent> kafkaSender;

    private static final String TOPIC = "order.events";

    public OrderEventPublisher(@Qualifier("jsonKafkaSender")KafkaSender<String, OrderEvent> kafkaSender) {
        this.kafkaSender = kafkaSender;
    }

    public Mono<Void> publish(OrderEvent event) {
        SenderRecord<String, OrderEvent, String> record =
                SenderRecord.create(
                        new ProducerRecord<>(TOPIC, event.eventId(), event),
                        event.eventId() // correlation metadata
                );

        return kafkaSender.send(Mono.just(record))
                .doOnNext(result -> {
                    if (result.exception() == null) {
                        log.info("✅ Published event: {}", result.correlationMetadata());
                    } else {
                        log.error("❌ Failed to publish: {}", result.exception().getMessage());
                    }
                })
                .then(); // complete when ack received
    }
}
