package com.saha.amit.configuration;

import com.saha.amit.dto.PaymentDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class MessageFunctions {

    private static final Log log = LogFactory.getLog(MessageFunctions.class);

    @Bean
    public Function<PaymentDto, PaymentDto> sms(){
        return paymentDto -> {
          log.info("Sending SMS with details :" + paymentDto.toString());
          return paymentDto;
        };
    }

    @Bean
    public Function<PaymentDto, PaymentDto> email(){
        return paymentDto -> {
            log.info("Sending SMS with details :" + paymentDto.toString());
            return paymentDto;
        };
    }
}
