package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

class ProductNotFoundException extends ResourceNotFoundException {

    private static final String RESOURCE_NAME = "Product";

    public ProductNotFoundException(String productName) {
        super(productName, RESOURCE_NAME);
    }

}
