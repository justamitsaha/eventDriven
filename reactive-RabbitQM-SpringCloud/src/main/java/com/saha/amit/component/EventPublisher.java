package com.saha.amit.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@Component
public class EventPublisher {

    private final Sender sender;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String routingKey;

    public EventPublisher(Sender sender,
                          ObjectMapper objectMapper,
                          @Value("${app.rabbit.exchange}") String exchange,
                          @Value("${app.rabbit.routingKey}") String routingKey) {
        this.sender = sender;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public <T> Mono<Void> publishEvent(String routingKey, T payload) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(payload);
            OutboundMessage msg = new OutboundMessage(exchange, routingKey, body);
            return sender.send(Mono.just(msg))
                    .doOnSuccess(r -> System.out.println("Published event to " + exchange + " : " + routingKey))
                    .doOnError(e -> System.err.println("Failed to publish: " + e.getMessage()));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    // convenience method for default routing key
    public <T> Mono<Void> publishEvent(T payload) {
        return publishEvent(this.routingKey, payload);
    }
}