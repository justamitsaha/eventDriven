package com.saha.amit.configuration;


import com.rabbitmq.client.ConnectionFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.rabbitmq.*;

@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.host:localhost}")
    private String host;

    @Value("${rabbitmq.port:5672}")
    private int port;

    @Value("${rabbitmq.username:guest}")
    private String username;

    @Value("${rabbitmq.password:guest}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory cf = new ConnectionFactory();
        cf.setHost(host);
        cf.setPort(port);
        cf.setUsername(username);
        cf.setPassword(password);
        // optional tuning:
        // cf.useNio();
        return cf;
    }

    @Bean
    public Sender sender(ConnectionFactory connectionFactory) {
        SenderOptions options = new SenderOptions()
                .connectionFactory(connectionFactory);
        return RabbitFlux.createSender(options);
    }

    @Bean
    public Receiver rabbitReceiver(ConnectionFactory connectionFactory) {
        ReceiverOptions options = new ReceiverOptions()
                .connectionFactory(connectionFactory);
        return RabbitFlux.createReceiver(options);
    }
}
