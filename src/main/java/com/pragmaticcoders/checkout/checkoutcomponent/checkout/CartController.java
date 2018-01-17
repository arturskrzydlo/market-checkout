package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController("/cart") class CartController {

    private CartService cartService;

    @PostMapping
    public Map<String, Double> scanProduct(@RequestBody ScannedProductDTO scannedProductDTO) {
        return Collections.singletonMap("price", cartService.addProductToCard(scannedProductDTO));
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }
}
