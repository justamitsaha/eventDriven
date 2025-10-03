package com.saha.amit.component.nonReactive;



import com.saha.amit.dto.PaymentDto;
import com.saha.amit.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedPublisherNonReactive {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCompletedPublisherNonReactive.class);

    private final RabbitTemplate rabbitTemplate;

    public PaymentCompletedPublisherNonReactive(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Consume payment.created, update status, emit payment.completed.
     */
    @RabbitListener(queues = "payment-service-queue")
    public void handlePaymentCreated(PaymentDto payment) {
        logger.info("💳 Consumed payment.created for order {}", payment.getOrderId());

        // Set status to COMPLETED
        payment.setStatus(Status.COMPLETED);

        // Emit payment.completed
        rabbitTemplate.convertAndSend(
                "order.events",       // exchange
                "payment.completed",  // routing key
                payment
        );

        logger.info("✅ Published payment.completed for order {}", payment.getOrderId());
    }
}

