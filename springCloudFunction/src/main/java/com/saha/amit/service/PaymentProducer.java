package com.saha.amit.service;

import com.saha.amit.dto.PaymentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class PaymentProducer {

    private final StreamBridge streamBridge;

    public PaymentProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendEmail(PaymentDto dto) {
        log.info("Producing to emailEvent: {}", dto);
        if (dto.getPaymentUuid() == null || dto.getPaymentUuid().isEmpty()) {
            String uuidString = UUID.randomUUID().toString();
            dto.setPaymentUuid(uuidString);
        }
        Message<PaymentDto> message = MessageBuilder
                .withPayload(dto)
                .setHeader(KafkaHeaders.KEY, String.valueOf(dto.getPaymentUuid()).getBytes())
                .build();
        streamBridge.send("emailProducer-out-0", message);
    }

    public void sendSms(PaymentDto dto) {
        log.info("Producing to smsEvent: {}", dto);
        if (dto.getPaymentUuid() == null || dto.getPaymentUuid().isEmpty()) {
            String uuidString = UUID.randomUUID().toString();
            dto.setPaymentUuid(uuidString);
        }
        Message<PaymentDto> message = MessageBuilder
                .withPayload(dto)
                .setHeader(KafkaHeaders.KEY, String.valueOf(dto.getPaymentUuid()).getBytes())
                .build();
        streamBridge.send("smsProducer-out-0", message);
    }

    public void sendSmsDlt(Message<PaymentDto> message) {
        log.info("Producing DLT messages to smsEvent: {}", message.getPayload());
        streamBridge.send("smsProducer-out-0", message);
    }

}

