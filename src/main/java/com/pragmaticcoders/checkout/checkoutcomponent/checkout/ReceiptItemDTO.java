package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString class ReceiptItemDTO {

    private ProductDTO product;
    private Integer receiptId;
    private Integer quantity;
    private Double price;
}
