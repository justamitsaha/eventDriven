package com.saha.amit.component.reactive;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.saha.amit.dto.DeliveryDto;
import com.saha.amit.dto.PaymentDto; // or DeliveryDto if you want a separate DTO
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.AcknowledgableDelivery;
import reactor.rabbitmq.Receiver;

@Component
public class DeliveryClosedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryClosedConsumer.class);

    private final Receiver receiver;
    private final ObjectMapper objectMapper;

    public DeliveryClosedConsumer(Receiver receiver, ObjectMapper objectMapper) {
        this.receiver = receiver;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void subscribe() {
        receiver.consumeManualAck("closure-service-queue")
                .flatMap(this::processDelivery)
                .doOnError(err -> logger.error("❌ Error in delivery.closed consumer", err))
                .subscribe();
    }

    private Mono<Void> processDelivery(AcknowledgableDelivery delivery) {
        try {
            DeliveryDto deliveryDto = objectMapper.readValue(delivery.getBody(), DeliveryDto.class);
            logger.info("🎉 Final Event → delivery.closed for order {}", deliveryDto.toString());
            delivery.ack();
            return Mono.empty();
        } catch (Exception e) {
            logger.error("❌ Failed to deserialize delivery.closed payload", e);
            delivery.nack(false); // send to DLQ if configured
            return Mono.empty();
        }
    }
}
