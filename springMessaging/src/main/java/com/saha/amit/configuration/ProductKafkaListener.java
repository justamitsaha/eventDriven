package com.saha.amit.configuration;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

@Configuration
@EnableKafka
public class ProductKafkaListener {

    Log log = LogFactory.getLog(ProductKafkaListener.class);
    @Autowired
    KafkaTemplate kafkaTemplate;
    @Value("${topics.retry}")
    private String retryTopic;
    @Value("${topics.dlt}")
    private String deadLetterTopic;

    public DeadLetterPublishingRecoverer publishingErrorScenarios() {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate
                , (r, e) -> {
            log.error("Exception in publishingRecoverer : {} " + e.getMessage());
            if (e.getCause() instanceof RecoverableDataAccessException) {
                log.info("SENDING TO RETRY TOPIC  " + e.getClass());
                return new TopicPartition(retryTopic, r.partition());
            } else {
                log.info("SENDING TO DEAD TOPIC  " + e.getClass());
                return new TopicPartition(deadLetterTopic, r.partition());
            }
        }
        );

        return recoverer;

    }

    public DefaultErrorHandler errorHandler() {
        //retries with Fixed backoff
        var fixedBackOff = new FixedBackOff(1000L, 1);
        var errorHandler = new DefaultErrorHandler(publishingErrorScenarios(), fixedBackOff);

        //retries with Exponential backoff
//        ExponentialBackOffWithMaxRetries expBackOff = new ExponentialBackOffWithMaxRetries(2);
//        expBackOff.setInitialInterval(1_000L);
//        expBackOff.setMultiplier(2.0);
//        expBackOff.setMaxInterval(2_000L);
//        errorHandler = new DefaultErrorHandler(expBackOff);

        //For additional logging
//        errorHandler.setRetryListeners(((record, ex, deliveryAttempt) -> {
//            log.error("Error in consuming product for "+ record +" error " +ex+ " deliveryAttempt "+deliveryAttempt );
//        }));

        // Ignore reties for specific exception
        var exceptionToIgnoreList = List.of(
                IllegalArgumentException.class,
                RecoverableDataAccessException.class
        );
        exceptionToIgnoreList.forEach(errorHandler::addNotRetryableExceptions);

        return errorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configure,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configure.configure(factory, kafkaConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(errorHandler());
        factory.setConcurrency(3);
        return factory;
    }

}
