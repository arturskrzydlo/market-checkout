package com.pragmaticcoders.checkout.checkoutcomponent.products;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Double findActualPriceForProduct(String productName) throws ProductNotFoundException;

    Product findProductByName(String productName) throws ProductNotFoundException;

    Optional<Product> createProduct(String name, Double price);

    List<Product> getAllProducts();
}
