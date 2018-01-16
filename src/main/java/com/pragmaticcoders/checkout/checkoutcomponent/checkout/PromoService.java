package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.stereotype.Service;

import java.util.List;

@Service interface PromoService {

    Promo createMultiPricedPromo(String productName, Integer unitAmount, Double specialPrice)
            throws ProductNotFoundException;

    List<Promo> getAllPromotionsForProduct(String productName) throws ProductNotFoundException;
}
