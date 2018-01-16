package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.stereotype.Service;

@Service interface PromoService {

    Promo createMultiPricedPromo(String productName, Integer unitAmount, Double specialPrice)
            throws ProductNotFoundException;
}
