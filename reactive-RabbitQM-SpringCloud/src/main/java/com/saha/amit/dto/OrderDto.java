package com.saha.amit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String orderId;
    private String customerId;
    private double amount;
    private String product;
}

