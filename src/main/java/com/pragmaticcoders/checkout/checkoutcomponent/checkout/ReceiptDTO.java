package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ReceiptDTO {

    private String receiptId;
    private Double payment;
}
