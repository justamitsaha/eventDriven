package com.saha.amit.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saha.amit.dto.PaymentDto;
import com.saha.amit.service.PaymentProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@AllArgsConstructor
@Configuration
public class MessageFunctions {

    private final ObjectMapper objectMapper;

    private final PaymentProducer paymentProducer;

    private final StreamBridge streamBridge;

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

                // 👇 Apply filter logic here
                if ("COMPLETED".equalsIgnoreCase(dto.getPaymentStatus())) {
                    log.warn("⛔ smsConsumer Dropping COMPLETED payment: {}", dto);
                    return; // drop the message, no further processing
                }

                log.info("Processed SMS DTO: {}", dto);

                if (dto.getAmount() <= 0) {
                    throw new RuntimeException("❌ Invalid amount: " + dto.getAmount());
                }

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

                // 👇 Apply filter logic here
                if ("COMPLETED".equalsIgnoreCase(dto.getPaymentStatus())) {
                    log.warn("⛔ emailConsumer Dropping COMPLETED payment: {}", dto);
                    return;
                }

                log.info("Processed Email DTO: {}", dto);

                if (dto.getAmount() <= 0) {
                    throw new RuntimeException("❌ Invalid amount: " + dto.getAmount());
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to process Email message", e);
            }
        };
    }


    @Bean
    public Function<Object, Object> filterNonCompletedPayments() {
        return payload -> {
            try {
                String json = payload instanceof byte[] ? new String((byte[]) payload) : payload.toString();
                PaymentDto dto = objectMapper.readValue(json, PaymentDto.class);

                log.info("🔍 Filter checking payment: {}", dto);
                if ("COMPLETED".equalsIgnoreCase(dto.getPaymentStatus())) {
                    log.warn("⛔ Dropping COMPLETED payment: {}", dto);
                    return null;
                }
                return payload;
            } catch (Exception e) {
                log.error("⚠️ Filter failed to parse message", e);
                return null;
            }
        };
    }


    @Bean
    public Consumer<Message<byte[]>> smsDlqConsumer() {
        return message -> {
            try {
                // Payload
                String json = new String(message.getPayload());
                PaymentDto dto = objectMapper.readValue(json, PaymentDto.class);

                // Headers (defensive conversion from byte[] -> String)
                String originalTopic = asString(message.getHeaders().get("x-original-topic"));
                String exceptionMessage = asString(message.getHeaders().get("x-exception-message"));
                Integer partition = message.getHeaders().get(KafkaHeaders.PARTITION, Integer.class);

                log.error("🚨 DLQ Consumer | From Topic={} | Partition={} | Error={} | Payload={}",
                        originalTopic, partition, exceptionMessage, dto);

                dto.setAmount(5000);

                // Re-send to original topic (smsEvent)
                Message<PaymentDto> retryMessage = MessageBuilder
                        .withPayload(dto)
                        .setHeader(KafkaHeaders.KEY, dto.getPaymentUuid().getBytes()) // ensure same partitioning
                        .build();

                paymentProducer.sendSmsDlt(retryMessage);

            } catch (Exception e) {
                log.error("⚠️ Failed to process DLQ message", e);
            }
        };
    }

    private String asString(Object headerValue) {
        if (headerValue == null) return null;
        if (headerValue instanceof byte[]) {
            return new String((byte[]) headerValue);
        }
        return headerValue.toString();
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