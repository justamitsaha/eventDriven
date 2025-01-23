package com.saha.amit.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.PaymentDto;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class RetryConsumer implements AcknowledgingMessageListener<String, String> {

    private final ObjectMapper objectMapper;
    Log log = LogFactory.getLog(RetryConsumer.class);

    public RetryConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @KafkaListener(topics = {"${topics.retry}"}, groupId = "${topics.retry.group}")
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        log.info("INSIDE RetryConsumer ");
        PaymentDto productDTO = null;
        try {
            productDTO = objectMapper.readValue(consumerRecord.value(), PaymentDto.class);
            log.info("RETRY RECEIVED --> " + productDTO + " RECORD ");
            consumerRecord.headers().forEach(header ->
                    log.info("KEY " + header.key() + " VALUE "+new String(header.value()))
            );
            assert acknowledgment != null;
            acknowledgment.acknowledge();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
