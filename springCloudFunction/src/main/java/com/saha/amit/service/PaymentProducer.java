package com.saha.amit.service;

import com.saha.amit.dto.PaymentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentProducer {

    private final StreamBridge streamBridge;

    public PaymentProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendEmail(PaymentDto dto) {
        log.info("Producing to emailEvent: {}", dto);
        streamBridge.send("emailProducer-out-0", dto);
    }

    public void sendSms(PaymentDto dto) {
        log.info("Producing to smsEvent: {}", dto);
        streamBridge.send("smsProducer-out-0", dto);
    }
}

