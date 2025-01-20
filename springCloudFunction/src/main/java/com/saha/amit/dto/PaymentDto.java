package com.saha.amit.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PaymentDto {
    private int paymentUuid;
    private String paymentStatus;
    private int amount;
}
