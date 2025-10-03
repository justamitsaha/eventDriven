package com.saha.amit.component.nonReactive;


import com.saha.amit.component.reactive.DeliveryStartedPublisher;
import com.saha.amit.dto.PaymentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedConsumerNonReactive {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCompletedConsumerNonReactive.class);

    private final DeliveryStartedPublisher deliveryStartedPublisher;

    public PaymentCompletedConsumerNonReactive(DeliveryStartedPublisher deliveryStartedPublisher) {
        this.deliveryStartedPublisher = deliveryStartedPublisher;
    }

    @RabbitListener(queues = "payment-completed-queue")
    public void handlePaymentCompleted(PaymentDto payment) {
        logger.info("✅ Consumed payment.completed for order {}", payment.getOrderId());

        // Publish delivery.started reactively
        deliveryStartedPublisher.publishDeliveryStarted(payment)
                .subscribe(); // trigger reactive pipeline
    }
}


