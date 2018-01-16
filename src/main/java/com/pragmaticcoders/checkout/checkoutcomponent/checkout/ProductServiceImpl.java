package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override public Double findActualPriceForProduct(String productName) throws ProductNotFoundException {
        Optional<Product> product = Optional.ofNullable(productRepository.findByName(productName));
        return product
                .map(product1 -> product1.getPrice())
                .orElseThrow(() -> new ProductNotFoundException(productName));
    }
}
