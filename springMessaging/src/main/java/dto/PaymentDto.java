package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PaymentDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String paymentUuid;
    @Schema(description = "Payment Status", example = "IN_PROGRESS")
    private PaymentStatus paymentStatus;
    @Schema(description = "Amount", example = "9999.99")
    private int amount;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdDate;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedDate;
    @Schema(description = "Payment Type", example = "CREDIT_CARD/UPI/COD/DEBIT_CARD/NET_BANKING")
    private PaymentType paymentType;
}
