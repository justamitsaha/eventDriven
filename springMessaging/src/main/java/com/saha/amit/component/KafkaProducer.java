package com.saha.amit.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.PaymentDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;


    private final Log log = LogFactory.getLog(KafkaTemplate.class);

    @Autowired
    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

//    public void sendRabbitMqCommunication(ProductDto productDto) {
//        log.info("Sending Communication request for the details: {} " + productDto);
//        var result = streamBridge.send("sendCommunication-out-0", productDto);
//        log.info("Is the Communication request successfully triggered ? : {} " + result);
//    }

    public CompletableFuture<SendResult<String, String>> sendKafkaEvent(PaymentDto paymentDto) throws JsonProcessingException {

        UUID uuid = UUID.randomUUID();
        paymentDto.setPaymentUuid(uuid.toString());
        paymentDto.setCreatedDate(LocalDateTime.now());
        String value = objectMapper.writeValueAsString(paymentDto);

        var completableFuture = kafkaTemplate.send("product", String.valueOf(paymentDto.getPaymentType()), value);
        return completableFuture.whenComplete((stringStringSendResult, throwable) ->
        {
            if (null != throwable)
                log.error("Error in sending to kafka");
            else
                log.info("Message successfully send to kafka" + stringStringSendResult);
        });
    }

    public SendResult<String, String> sendKafkaEvent2(PaymentDto paymentDto) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        UUID uuid = UUID.randomUUID();
        paymentDto.setPaymentUuid(uuid.toString());
        paymentDto.setCreatedDate(LocalDateTime.now());
        String value = objectMapper.writeValueAsString(paymentDto);
        //Blocking call
        return kafkaTemplate.send("product", String.valueOf(paymentDto.getPaymentType()), value).get(3, TimeUnit.SECONDS);
    }

    public CompletableFuture<SendResult<String, String>> sendKafkaEvent3(PaymentDto paymentDto) throws JsonProcessingException {

        UUID uuid = UUID.randomUUID();
        paymentDto.setPaymentUuid(uuid.toString());
        paymentDto.setCreatedDate(LocalDateTime.now());
        String value = objectMapper.writeValueAsString(paymentDto);

        ProducerRecord<String, String> producerRecord = buildProducerRecord(paymentDto.getPaymentUuid(), value, "product");

        var completableFuture = kafkaTemplate.send("product", String.valueOf(paymentDto.getPaymentType()), value);
        return completableFuture.whenComplete((stringStringSendResult, throwable) ->
        {
            if (null != throwable)
                log.error("Error in sending to kafka");
            else
                log.info("Message successfully send to kafka" + stringStringSendResult);
        });
    }

    private ProducerRecord<String, String> buildProducerRecord(String key, String value, String topic) {
        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(topic, null, key, value, recordHeaders);
    }
}
