package com.pragmaticcoders.checkout.checkoutcomponent.products;

import org.springframework.data.repository.CrudRepository;

interface ProductRepository extends CrudRepository<Product, Integer> {

    Product findByName(String productName);
}
