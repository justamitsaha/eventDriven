package com.saha.amit.consumer;

import com.saha.amit.services.PaymentService;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer implements AcknowledgingMessageListener<String, String> {
    private final PaymentService productConsumerService;
    Log log = LogFactory.getLog(PaymentConsumer.class);

    @Autowired
    public PaymentConsumer(PaymentService productConsumerService) {
        this.productConsumerService = productConsumerService;
    }



    @Override
    @KafkaListener(topics = {"${topic.main}"}, groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        log.info("CONSUMED RECORD IN ProductKafkaConsumer");
        if (productConsumerService.processRecord(consumerRecord))
            acknowledgment.acknowledge();
    }
}
