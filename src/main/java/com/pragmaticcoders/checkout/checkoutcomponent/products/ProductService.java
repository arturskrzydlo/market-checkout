package com.pragmaticcoders.checkout.checkoutcomponent.products;

import java.util.List;

public interface ProductService {

    Double findActualPriceForProduct(String productName) throws ProductNotFoundException;

    Product findProductByName(String productName) throws ProductNotFoundException;

    List<Product> getAllProducts();
}
