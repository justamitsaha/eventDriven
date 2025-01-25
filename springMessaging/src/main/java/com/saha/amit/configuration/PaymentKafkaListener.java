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
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

@Configuration
@EnableKafka
public class PaymentKafkaListener {

    Log log = LogFactory.getLog(PaymentKafkaListener.class);
    @Autowired
    KafkaTemplate kafkaTemplate;
    @Value("${topics.retry}")
    private String retryTopic;
    @Value("${topics.dlt}")
    private String deadLetterTopic;

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configure,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configure.configure(factory, kafkaConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);    // Acknowledgement
        factory.setCommonErrorHandler(errorHandler());
        factory.setConcurrency(3);          //No of concurrent listeners
        return factory;
    }

    public DefaultErrorHandler errorHandler() {
        //retries with Fixed backoff
        var fixedBackOff = new FixedBackOff(1000L, 2);
        var errorHandler = new DefaultErrorHandler(publishingErrorScenarios(), fixedBackOff);

        //retries with Exponential backoff
        ExponentialBackOffWithMaxRetries expBackOff = new ExponentialBackOffWithMaxRetries(2);
        expBackOff.setInitialInterval(1_000L);      //first retry duration
        expBackOff.setMultiplier(2.0);              //subsequent retry attempt multiplied by
        expBackOff.setMaxInterval(2_000L);          //Max wait duration
        errorHandler = new DefaultErrorHandler(publishingErrorScenarios(), expBackOff);

        //For additional logging If you'd like to see what's happening in each and every attempt
        errorHandler.setRetryListeners(((record, ex, deliveryAttempt) -> {
            log.error("Error in consuming product for " + record + " error " + ex + " deliveryAttempt " + deliveryAttempt);
        }));

        // Ignore reties for specific exception
        var exceptionToIgnoreList = List.of(
                IllegalArgumentException.class,
                RecoverableDataAccessException.class
        );
        exceptionToIgnoreList.forEach(errorHandler::addNotRetryableExceptions);

        return errorHandler;
    }

    public DeadLetterPublishingRecoverer publishingErrorScenarios() {

        return new DeadLetterPublishingRecoverer(kafkaTemplate
                , (r, e) -> {
            log.error("Exception in publishing message : {} " + e.getMessage());
            if (e.getCause() instanceof RecoverableDataAccessException) {
                log.info("SENDING TO RETRY TOPIC  " + e.getClass());
                return new TopicPartition(retryTopic, r.partition());
            } else {
                log.info("SENDING TO DEAD TOPIC  " + e.getClass());
                return new TopicPartition(deadLetterTopic, r.partition());
            }
        });

    }


}
