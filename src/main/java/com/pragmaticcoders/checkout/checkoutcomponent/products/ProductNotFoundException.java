package com.pragmaticcoders.checkout.checkoutcomponent.products;

import com.pragmaticcoders.checkout.checkoutcomponent.general.errors.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {

    private static final String RESOURCE_NAME = "Product";

    public ProductNotFoundException(String productName) {
        super(productName, RESOURCE_NAME);
    }

    public ProductNotFoundException() {
        super("NO_NAME", RESOURCE_NAME);
    }
}
