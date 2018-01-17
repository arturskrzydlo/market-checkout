package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products") class ProductController {

    @GetMapping
    public List<Product> getAllProducts() {

        return new ArrayList<>();

    }

}
