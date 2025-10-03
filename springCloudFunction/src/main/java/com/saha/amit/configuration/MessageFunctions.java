package com.saha.amit.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saha.amit.dto.PaymentDto;
import com.saha.amit.service.PaymentProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@AllArgsConstructor
@Configuration
public class MessageFunctions {

    private final ObjectMapper objectMapper;

    private final PaymentProducer paymentProducer;

    @Bean
    public Function<Message<byte[]>, PaymentDto> smsProducer() {
        return message -> {
            try {
                String json = new String(message.getPayload()); // bytes → JSON string
                log.info("Raw JSON sms message : {}", json);
                PaymentDto dto = objectMapper.readValue(json, PaymentDto.class); // JSON → DTO
                paymentProducer.sendSms(dto);
                log.info("Converted SMS DTO: {}", dto);
                return dto;
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert SMS message", e);
            }
        };
    }

    @Bean
    public Function<Message<byte[]>, PaymentDto> emailProducer() {
        return message -> {
            try {
                String json = new String(message.getPayload());
                log.info("Raw JSON email message : {}", json);
                PaymentDto dto = objectMapper.readValue(json, PaymentDto.class);
                paymentProducer.sendEmail(dto);
                log.info("Converted Email DTO: {}", dto);
                return dto;
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert Email message", e);
            }
        };
    }

    @Bean
    public Consumer<Message<byte[]>> smsConsumer() {
        return message -> {
            try {
                String json = new String(message.getPayload());
                log.info("Raw JSON message smsConsumer: {}", json);
                PaymentDto dto = objectMapper.readValue(json, PaymentDto.class);
                log.info("Processed SMS DTO: {}", dto);
            } catch (Exception e) {
                throw new RuntimeException("Failed to process SMS message", e);
            }
        };
    }

    @Bean
    public Consumer<Message<byte[]>> emailConsumer() {
        return message -> {
            try {
                String json = new String(message.getPayload());
                log.info("Raw JSON message emailConsumer: {}", json);
                PaymentDto dto = objectMapper.readValue(json, PaymentDto.class);
                log.info("Processed Email DTO: {}", dto);
            } catch (Exception e) {
                throw new RuntimeException("Failed to process Email message", e);
            }
        };
    }


}


/*More straightforward version without Message<byte[]> handling
    // --- PRODUCERS ---
    @Bean
    public Function<PaymentDto, PaymentDto> smsProducer() {
        return dto -> {
            log.info("Producing SMS DTO: {}", dto);
            return dto; // Will be serialized to JSON
        };
    }

    @Bean
    public Function<PaymentDto, PaymentDto> emailProducer() {
        return dto -> {
            log.info("Producing Email DTO: {}", dto);
            return dto; // Will be serialized to JSON
        };
    }

    // --- CONSUMERS ---
    @Bean
    public Consumer<PaymentDto> smsConsumer() {
        return dto -> {
            log.info("Consumed SMS DTO: {}", dto);
        };
    }

    @Bean
    public Consumer<PaymentDto> emailConsumer() {
        return dto -> {
            log.info("Consumed Email DTO: {}", dto);
        };
    }
 */