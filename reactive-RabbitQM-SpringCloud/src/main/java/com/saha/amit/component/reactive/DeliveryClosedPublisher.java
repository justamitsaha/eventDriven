package com.saha.amit.component.reactive;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.saha.amit.dto.DeliveryDto;
import com.saha.amit.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@Service
public class DeliveryClosedPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryClosedPublisher.class);

    private final Sender sender;
    private final ObjectMapper objectMapper;
    private final String exchange;

    public DeliveryClosedPublisher(Sender sender,
                                   ObjectMapper objectMapper,
                                   @Value("${app.rabbit.exchange}") String exchange) {
        this.sender = sender;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
    }

    public Mono<Void> publishDeliveryClosed(DeliveryDto deliveryDto) {
        try {
            deliveryDto.setStatus(Status.COMPLETED);
            byte[] body = objectMapper.writeValueAsBytes(deliveryDto);
            OutboundMessage msg = new OutboundMessage(exchange, "delivery.closed", body);
            return sender.send(Mono.just(msg))
                    .doOnSuccess(v -> logger.info("📦✅ Published delivery.closed for order {}", deliveryDto.toString()))
                    .doOnError(e -> logger.error("❌ Failed to publish delivery.closed", e));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}

