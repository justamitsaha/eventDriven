package com.saha.amit.controller;

import dto.PaymentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("payment")
public class EventController {

    @PostMapping()
    public ResponseEntity<PaymentDto> create(@RequestBody PaymentDto paymentDto){
        paymentDto.setCreatedDate(LocalDateTime.now());
        return ResponseEntity.ok().body(paymentDto);
    }

}
