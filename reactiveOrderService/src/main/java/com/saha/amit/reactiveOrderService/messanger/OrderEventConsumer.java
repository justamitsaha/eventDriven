package com.saha.amit.reactiveOrderService.messanger;

import com.saha.amit.reactiveOrderService.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.kafka.receiver.KafkaReceiver;

@Slf4j
@Service
public class OrderEventConsumer {

    private final KafkaReceiver<String, OrderEvent> kafkaReceiver;

    public OrderEventConsumer(@Qualifier("jsonKafkaReceiver") KafkaReceiver<String, OrderEvent> kafkaReceiver) {
        this.kafkaReceiver = kafkaReceiver;
    }

    public void consume() {
        Disposable subscription = kafkaReceiver
                .receive() // Flux<ReceiverRecord<K,V>>
                .doOnNext(record -> {
                    try {
                        OrderEvent event = record.value();
                        log.info("\uD83D\uDCE5 Received event: {}", event);

                        // ✅ Process business logic
                        handleEvent(event);

                        // ✅ Manual acknowledgement (commit offset)
                        record.receiverOffset().acknowledge();

                    } catch (Exception ex) {
                        log.error("❌ Error processing event: {}", ex.getMessage());
                        // Do NOT ack if processing failed → message will be redelivered
                    }
                })
                .subscribe();
    }

    private void handleEvent(OrderEvent event) {
        // Update order status in DB reactively, etc.
    }
}
