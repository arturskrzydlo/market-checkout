package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

interface ProductService {

    Double findActualPriceForProduct(String productName) throws ProductNotFoundException;

    Double countProductPriceWithPromotions(String productName, int quantity) throws ProductNotFoundException;
}
