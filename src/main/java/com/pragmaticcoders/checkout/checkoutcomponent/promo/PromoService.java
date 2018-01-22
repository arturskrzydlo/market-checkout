package com.pragmaticcoders.checkout.checkoutcomponent.promo;

import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductNotFoundException;

import java.util.List;

public interface PromoService {

    Promo createMultiPricedPromo(String productName, Integer unitAmount, Double specialPrice)
            throws ProductNotFoundException;

    List<Promo> getAllPromotionsForProduct(String productName) throws ProductNotFoundException;
}
