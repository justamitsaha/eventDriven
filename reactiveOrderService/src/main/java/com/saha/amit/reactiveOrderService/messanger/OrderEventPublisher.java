package com.saha.amit.reactiveOrderService.messanger;

import com.saha.amit.reactiveOrderService.events.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@Service
public class OrderEventPublisher {

    private final KafkaSender<String, OrderEvent> kafkaSender;
    private final DltPublisher dltPublisher;

    @Value("${app.kafka.topic.order}")
    private String orderTopic;

    @Value("${app.kafka.topic.order.dlt}")
    private String orderDltTopic;

    public OrderEventPublisher(@Qualifier("jsonKafkaSender") KafkaSender<String, OrderEvent> kafkaSender, DltPublisher dltPublisher) {
        this.kafkaSender = kafkaSender;
        this.dltPublisher = dltPublisher;
    }

    public Mono<Void> publish(OrderEvent event) {
        SenderRecord<String, OrderEvent, String> record =
                SenderRecord.create(
                        new ProducerRecord<>(orderTopic, event.eventId(), event),
                        event.eventId() // correlation metadata
                );

        return kafkaSender.send(Mono.just(record))
                .flatMap(result -> {
                    if (result.exception() == null) {
                        RecordMetadata metadata = result.recordMetadata();
                        log.info("✅ Published eventId={} to topic={}, partition={}, offset={}",
                                result.correlationMetadata(),
                                metadata.topic(),
                                metadata.partition(),
                                metadata.offset());
                        return Mono.empty();

                    } else {
                        return dltPublisher.sendToDlt(
                                event,
                                "producer",
                                result.exception().getClass().getSimpleName(),
                                orderDltTopic,
                                null,
                                null
                        );
                    }
                })
                .then(); // complete when ack received
    }
}
