package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString class ReceiptDTO {

    private String receiptId;
    private Double payment;
    private List<ReceiptItemDTO> items;
}
