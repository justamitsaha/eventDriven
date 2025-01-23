package com.saha.amit.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.PaymentDto;
import dto.PaymentType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    Log log = LogFactory.getLog(PaymentService.class);
    private final ObjectMapper objectMapper;

    public PaymentService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean processRecord(ConsumerRecord<String, String> consumerRecord) {
        PaymentDto paymentDto = null;
        try {
            paymentDto = objectMapper.readValue(consumerRecord.value(), PaymentDto.class);
            log.info("RECEIVED PRODUCT IN MAIN CONSUMER --> " + paymentDto + " RECORD " + consumerRecord);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (null != paymentDto) {
            if (paymentDto.getPaymentType().equals(PaymentType.COD))
                throw new IllegalArgumentException("Chinese mal fuck off");
            if (paymentDto.getAmount() > 99)
                throw new RecoverableDataAccessException("");
            else {
                log.info("PRODUCT PROCESSED SUCCESSFULLY --> " + paymentDto + " RECORD " + consumerRecord);
                log.info("SENDING ACKNOWLEDGEMENT  " + paymentDto.getPaymentUuid());
                return true;
            }
        } else
            throw new RuntimeException("Where is data ?");
    }
}
