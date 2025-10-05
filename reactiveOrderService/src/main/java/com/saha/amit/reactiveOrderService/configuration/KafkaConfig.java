package com.saha.amit.reactiveOrderService.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saha.amit.reactiveOrderService.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private static final String BOOTSTRAP = "localhost:9092";
    private final ObjectMapper objectMapper;

    // ---------- JSON CONFIG ----------

    @Bean("jsonKafkaSender")
    public KafkaSender<String, OrderEvent> jsonKafkaSender() {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer",
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer",
                ProducerConfig.ACKS_CONFIG, "all"
        );

        // custom serialization
        SenderOptions<String, OrderEvent> senderOptions =
                SenderOptions.<String, OrderEvent>create(props)
                        .withValueSerializer((topic, data) -> {
                            try {
                                return objectMapper.writeValueAsBytes(data);
                            } catch (Exception e) {
                                throw new RuntimeException("JSON serialization failed", e);
                            }
                        });

        return KafkaSender.create(senderOptions);
    }

    @Bean("jsonKafkaReceiver")
    public KafkaReceiver<String, OrderEvent> jsonKafkaReceiver() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP,
                ConsumerConfig.GROUP_ID_CONFIG, "order-service-json",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer",
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false // manual ack
        );

        ReceiverOptions<String, OrderEvent> receiverOptions =
                ReceiverOptions.<String, OrderEvent>create(props)
                        .subscription(Collections.singleton("order.events.json"))
                        .withValueDeserializer((topic, bytes) -> {
                            try {
                                return objectMapper.readValue(bytes, OrderEvent.class);
                            } catch (Exception e) {
                                throw new RuntimeException("JSON deserialization failed", e);
                            }
                        });

        return KafkaReceiver.create(receiverOptions);
    }


    // ---------- AVRO CONFIG ----------

    @Bean("avroKafkaSender")
    public KafkaSender<String, OrderEvent> avroKafkaSender() {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer",
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer",
                "schema.registry.url", "http://localhost:8081",
                ProducerConfig.ACKS_CONFIG, "all"
        );
        return KafkaSender.create(SenderOptions.<String, OrderEvent>create(props));
    }

    @Bean("avroKafkaReceiver")
    public KafkaReceiver<String, OrderEvent> avroKafkaReceiver() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP,
                ConsumerConfig.GROUP_ID_CONFIG, "order-service-avro",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer",
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer",
                "schema.registry.url", "http://localhost:8081",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
        );
        return KafkaReceiver.create(ReceiverOptions.<String, OrderEvent>create(props)
                .subscription(Collections.singleton("order.events.avro")));
    }
}

