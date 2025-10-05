package com.saha.amit.reactiveOrderService.messanger;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class DltPublisher {

    private final KafkaSender<String, byte[]> kafkaSender;
    private final ObjectMapper objectMapper;

    private static final String DLT_TOPIC = "order.events.dlt";

    public DltPublisher(@Qualifier("jsonKafkaReceiver")KafkaSender<String, byte[]> kafkaSender, ObjectMapper objectMapper) {
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> sendToDlt(OrderEvent event, String failureType, String reason, String sourceTopic, Integer partition, Long offset) {
        try {
            byte[] payload = objectMapper.writeValueAsBytes(event);

            ProducerRecord<String, byte[]> record = new ProducerRecord<>(DLT_TOPIC, event.customerId(), payload);

            // add metadata as headers
            record.headers()
                    .add("failure-type", failureType.getBytes())
                    .add("failure-reason", reason.getBytes())
                    .add("source-topic", sourceTopic.getBytes())
                    .add("source-partition", String.valueOf(partition != null ? partition : -1).getBytes())
                    .add("source-offset", String.valueOf(offset != null ? offset : -1).getBytes());

            SenderRecord<String, byte[], String> senderRecord =
                    SenderRecord.create(record, event.eventId());

            return kafkaSender.send(Mono.just(senderRecord))
                    .doOnNext(result -> {
                        if (result.exception() == null) {
                            log.error("☠️ EventId={} sent to DLT, type={}, reason={}", event.eventId(), failureType, reason);
                        } else {
                            log.error("🔥 DLT publish failed for eventId={} error={}", event.eventId(), result.exception().getMessage());
                        }
                    })
                    .then();

        } catch (Exception e) {
            log.error("🚨 Failed to serialize event for DLT", e);
            return Mono.empty();
        }
    }
}
