package com.pragmaticcoders.checkout.checkoutcomponent.promo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class PromoDTO {

    private Double specialPrice;
    private int unitAmount;
    private PromoType type;

}
