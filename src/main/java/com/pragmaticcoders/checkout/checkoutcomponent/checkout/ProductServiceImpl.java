package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override public Double findActualPriceForProduct(String productName) {
        Optional<Product> product = Optional.ofNullable(productRepository.findByName(productName));
        return product.map(product1 -> product1.getPrice()).orElse(0.0);
    }
}
