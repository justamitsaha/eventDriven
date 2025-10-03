package com.saha.amit.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DeliveryDto {
    private String orderId;
    private String customerId;
    private double amount;
    private Status status; // use enum instead of String
}
