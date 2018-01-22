package com.pragmaticcoders.checkout.checkoutcomponent.products;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ProductDTO {

    private String name;
    private Double price;
}
