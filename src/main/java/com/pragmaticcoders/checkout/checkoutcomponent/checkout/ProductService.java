package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import java.util.List;
import java.util.Set;

interface ProductService {

    Double findActualPriceForProduct(String productName) throws ProductNotFoundException;

    Double countProductPriceWithPromotions(String productName, int quantity) throws ProductNotFoundException;

    Double countProductPriceWithPromotions(Product product, int quantity, Set<ReceiptItem> allProductsInReceipt);

    List<Product> getAllProducts();
}
