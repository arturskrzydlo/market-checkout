package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.data.repository.CrudRepository;

interface ProductRepository extends CrudRepository<Product, Integer> {

    Product findByName(String productName);
}
