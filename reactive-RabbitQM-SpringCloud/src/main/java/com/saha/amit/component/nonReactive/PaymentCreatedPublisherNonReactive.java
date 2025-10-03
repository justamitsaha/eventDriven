package com.saha.amit.component.nonReactive;


import com.saha.amit.dto.OrderDto;
import com.saha.amit.dto.PaymentDto;
import com.saha.amit.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentCreatedPublisherNonReactive {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCreatedPublisherNonReactive.class);

    private final RabbitTemplate rabbitTemplate;

    public PaymentCreatedPublisherNonReactive(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentCreated(OrderDto order) {
        PaymentDto payment = new PaymentDto();
        payment.setOrderId(order.getOrderId());
        payment.setCustomerId(order.getCustomerId());
        payment.setAmount(order.getAmount());
        payment.setStatus(Status.PENDING); // ✅ use enum
        try {
            rabbitTemplate.convertAndSend(
                    "order.events",       // exchange
                    "payment.created",    // routing key
                    payment                 // message payload
            );
            logger.info("✅ Published payment.created for order {}", order.getOrderId());
        } catch (Exception e) {
            logger.error("❌ Failed to publish payment.created for order {}", order.getOrderId(), e);
            throw e; // let consumer decide whether to ack/nack
        }
    }
}
