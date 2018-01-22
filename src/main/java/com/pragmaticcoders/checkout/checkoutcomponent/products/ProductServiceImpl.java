package com.pragmaticcoders.checkout.checkoutcomponent.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service class ProductServiceImpl implements ProductService {

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

    @Override public Product findProductByName(String productName) throws ProductNotFoundException {
        Optional<Product> product = Optional.ofNullable(productRepository.findByName(productName));
        return product.orElseThrow(() -> new ProductNotFoundException(productName));
    }

    @Override public List<Product> getAllProducts() {
        return StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }


}
