package com.saha.amit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReactiveRabbitMqSpringCloudApplication {

    private  final static Logger logger = LoggerFactory.getLogger(ReactiveRabbitMqSpringCloudApplication.class);

    public static void main(String[] args) {
        String rabbitMq = "http://192.168.0.143:15672/";
        SpringApplication.run(ReactiveRabbitMqSpringCloudApplication.class, args);
        logger.info("Rabbit MQ, {} ",rabbitMq);
    }
}