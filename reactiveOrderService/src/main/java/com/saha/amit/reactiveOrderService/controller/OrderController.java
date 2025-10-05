package com.saha.amit.reactiveOrderService.controller;

import com.saha.amit.reactiveOrderService.dto.OrderRequest;
import com.saha.amit.reactiveOrderService.dto.OrderResponse;
import com.saha.amit.reactiveOrderService.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Mono<OrderResponse>> placeOrder(@RequestBody OrderRequest req) {
        logger.info("Received order placement request: {}", req);
        // Simulate order processing and response
        return ResponseEntity.ok(
                orderService.placeOrder(req.getCustomerId(), req.getAmount())
                        .map(event -> new OrderResponse(
                                event.orderId(),
                                event.customerId(),
                                event.amount(),
                                event.status()
                        ))
        );
    }

}
