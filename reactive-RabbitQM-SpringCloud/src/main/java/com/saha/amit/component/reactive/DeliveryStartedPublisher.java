package com.saha.amit.component.reactive;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.saha.amit.dto.DeliveryDto;
import com.saha.amit.dto.PaymentDto;
import com.saha.amit.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@Service
public class DeliveryStartedPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryStartedPublisher.class);

    private final Sender sender;
    private final ObjectMapper objectMapper;
    private final String exchange;

    public DeliveryStartedPublisher(Sender sender,
                                    ObjectMapper objectMapper,
                                    @Value("${app.rabbit.exchange}") String exchange) {
        this.sender = sender;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
    }

    public Mono<Void> publishDeliveryStarted(PaymentDto payment) {
        try {
            DeliveryDto delivery  = new DeliveryDto(payment.getOrderId(), payment.getCustomerId(), payment.getAmount(), Status.PENDING);
            logger.info("Emitting delivery.started for order {}", delivery.toString());
            byte[] body = objectMapper.writeValueAsBytes(delivery);
            OutboundMessage message = new OutboundMessage(exchange, "delivery.started", body);
            return sender.send(Mono.just(message))
                    .doOnSuccess(v -> logger.info("📦 Published delivery.started for order {}", delivery.toString()))
                    .doOnError(e -> logger.error("❌ Failed to publish delivery.started", e));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
