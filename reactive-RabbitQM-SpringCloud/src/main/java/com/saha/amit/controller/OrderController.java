package com.saha.amit.controller;

import com.saha.amit.model.Order;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("order")
public class OrderController {

    @Operation(summary = "Place a new order")
    @PostMapping
    public Mono<ResponseEntity<Order>> updateProduct(@RequestBody Order order){
        return Mono.just(ResponseEntity.ok(order));
    }
}
