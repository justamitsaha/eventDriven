package com.saha.amit.controller;

import com.saha.amit.component.reactive.OrderCreatedPublisherReactive;
import com.saha.amit.dto.OrderDto;
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

    private final OrderCreatedPublisherReactive orderCreatedPublisherReactive;

    public OrderController(OrderCreatedPublisherReactive orderCreatedPublisherReactive) {
        this.orderCreatedPublisherReactive = orderCreatedPublisherReactive;
    }

    @Operation(summary = "Place a new order")
    @PostMapping
    public Mono<ResponseEntity<OrderDto>> updateProduct(@RequestBody OrderDto order){
        return orderCreatedPublisherReactive.publishEvent("order.created", order)
                .then(Mono.just(ResponseEntity.ok(order)));
    }
}
