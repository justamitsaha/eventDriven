package com.saha.amit.reactiveOrderService.messanger;

import com.saha.amit.reactiveOrderService.events.OrderEvent;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;

@Slf4j
@Service
public class OrderEventConsumer {

    private final KafkaReceiver<String, OrderEvent> kafkaReceiver;
    private final DltPublisher dltPublisher;

    public OrderEventConsumer(@Qualifier("jsonKafkaReceiver") KafkaReceiver<String, OrderEvent> kafkaReceiver, DltPublisher dltPublisher) {
        this.kafkaReceiver = kafkaReceiver;
        this.dltPublisher = dltPublisher;
    }

    @PostConstruct
    public void consume() {
        log.info("Starting to consume events");
        Disposable subscription = kafkaReceiver
                .receive() // Flux<ReceiverRecord<K,V>>
                .flatMap(record -> {
                    OrderEvent event = record.value();
                    try {

                        log.info("📥 Received eventId={} from topic={}, partition={}, offset={}", event.eventId(), record.topic(), record.partition(), record.offset());
                        log.info("\uD83D\uDCE5 Received event: {}", event);
                        if (event.amount() != null && event.amount() > 0){
                            // ✅ Process business logic
                            handleEvent(event);
                        } else {
                            throw new IllegalArgumentException("Invalid amount: " + event.amount());
                        }
                        // ✅ Manual acknowledgement (commit offset)
                        record.receiverOffset().acknowledge();
                        return Mono.empty();
                    } catch (Exception ex) {
                        log.error("❌ Error processing event: {}", ex.getMessage());
                        // Do NOT ack if processing failed → message will be redelivered
                        return dltPublisher.sendToDlt(
                                event,
                                "consumer",
                                ex.getClass().getSimpleName(),
                                record.topic(),
                                record.partition(),
                                record.offset()
                        ).then(Mono.fromRunnable(record.receiverOffset()::acknowledge));
                    }
                })
                .subscribe();
    }

    private void handleEvent(OrderEvent event) {
        log.info("Handling event: {}", event);
    }
}
