package com.saha.amit.reactiveOrderService.repository;

import com.saha.amit.reactiveOrderService.model.OrderEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, String>, OrderRepositoryCustom {
}
