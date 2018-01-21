package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString class ProductDTO {

    private String name;
    private Double price;
}
