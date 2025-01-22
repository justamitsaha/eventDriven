package com.saha.amit.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PaymentDto {
    private int paymentUuid;
    private String paymentStatus;
    private int amount;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
