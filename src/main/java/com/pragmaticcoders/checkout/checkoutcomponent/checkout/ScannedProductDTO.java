package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data class ScannedProductDTO {

    @JsonProperty("product_name")
    private String productName;
    private int quantity;

}
