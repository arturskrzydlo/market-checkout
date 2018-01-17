package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import java.util.List;

interface ProductService {

    Double findActualPriceForProduct(String productName) throws ProductNotFoundException;

    Double countProductPriceWithPromotions(String productName, int quantity) throws ProductNotFoundException;

    List<Product> getAllProducts();
}
