package com.pragmaticcoders.checkout.checkoutcomponent.products;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pragmaticcoders.checkout.checkoutcomponent.promo.PromoDTO;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ProductDTO {

    private String name;
    private Double price;
    private Set<PromoDTO> promos;
}
