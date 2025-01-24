package com.saha.amit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.saha.amit.component.KafkaProducer;
import dto.PaymentDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("payment")
public class EventController {

    private final KafkaProducer kafkaProducer;

    private final Log log = LogFactory.getLog(EventController.class);

    @Autowired
    public EventController(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping()
    public ResponseEntity<PaymentDto> create(@RequestBody PaymentDto paymentDto) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        paymentDto.setCreatedDate(LocalDateTime.now());
        int random = (new Random()).nextInt(1, 4);
        switch (random) {
            case 1 -> {
                var result = kafkaProducer.sendKafkaEvent(paymentDto);
                log.info("Message send sendKafkaEvent " + result);
            }
            case 2 -> {
                var result = kafkaProducer.sendKafkaEvent2(paymentDto);
                log.info("Message send sendKafkaEvent " + result);
            }
            case 3 -> {
                var result = kafkaProducer.sendKafkaEvent3(paymentDto);
                log.info("Message send sendKafkaEvent " + result);
            }
        };
        return ResponseEntity.ok().body(paymentDto);
    }

}
