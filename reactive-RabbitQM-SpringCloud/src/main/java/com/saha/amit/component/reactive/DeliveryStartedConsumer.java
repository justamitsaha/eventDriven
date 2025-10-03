package com.saha.amit.component.reactive;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.saha.amit.dto.DeliveryDto;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.AcknowledgableDelivery;
import reactor.rabbitmq.Receiver;

@Component
public class DeliveryStartedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryStartedConsumer.class);

    private final Receiver receiver;
    private final ObjectMapper objectMapper;
    private final DeliveryClosedPublisher deliveryClosedPublisher;

    public DeliveryStartedConsumer(Receiver receiver,
                                   ObjectMapper objectMapper,
                                   DeliveryClosedPublisher deliveryClosedPublisher) {
        this.receiver = receiver;
        this.objectMapper = objectMapper;
        this.deliveryClosedPublisher = deliveryClosedPublisher;
    }

    @PostConstruct
    public void subscribe() {
        receiver.consumeManualAck("delivery-service-queue")
                .flatMap(this::processDelivery)
                .doOnError(err -> logger.error("❌ Error in delivery.completed consumer", err))
                .subscribe();
    }

    private Mono<Void> processDelivery(AcknowledgableDelivery delivery) {
        try {
            DeliveryDto deliveryDto = objectMapper.readValue(delivery.getBody(), DeliveryDto.class);
            logger.info("📦 Consumed delivery.completed for order {}", deliveryDto.toString());

            // emit delivery.closed
            return deliveryClosedPublisher.publishDeliveryClosed(deliveryDto)
                    .doOnSuccess(v -> {
                        delivery.ack();
                        logger.info("✅ Acked delivery.completed and emitted delivery.closed for order {}", deliveryDto.toString());
                    })
                    .doOnError(e -> {
                        logger.error("❌ Failed to publish delivery.closed", e);
                        delivery.nack(false); // goes to DLQ if configured
                    })
                    .onErrorResume(e -> Mono.empty());
        } catch (Exception e) {
            logger.error("❌ Failed to deserialize delivery.completed payload", e);
            delivery.nack(false);
            return Mono.empty();
        }
    }
}
