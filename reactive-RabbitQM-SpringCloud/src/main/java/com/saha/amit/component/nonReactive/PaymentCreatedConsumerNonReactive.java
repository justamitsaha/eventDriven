package com.saha.amit.component.nonReactive;


import com.saha.amit.dto.PaymentDto;
import com.saha.amit.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentCreatedConsumerNonReactive {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCreatedConsumerNonReactive.class);

    private final PaymentCompletedPublisherNonReactive paymentCompletedPublisherNonReactive;

    public PaymentCreatedConsumerNonReactive(PaymentCompletedPublisherNonReactive paymentCompletedPublisherNonReactive) {
        this.paymentCompletedPublisherNonReactive = paymentCompletedPublisherNonReactive;
    }


    @RabbitListener(queues = "payment-service-queue")
    public void handlePaymentCreated(PaymentDto paymentDto) {
        logger.info("💳 Consumed payment.created for order {}", paymentDto.getOrderId());
        // Create a Payment record (simulate processing)
        paymentDto.setStatus(Status.COMPLETED); // ✅ use enum
        // After processing → emit payment.completed
        paymentCompletedPublisherNonReactive.handlePaymentCreated(paymentDto);
    }
}
