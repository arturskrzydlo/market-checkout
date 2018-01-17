package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import java.text.MessageFormat;

class ProductNotFoundException extends Exception {

    private static final String MESSAGE = "Product with name {0} does not exists";
    String productName;

    public ProductNotFoundException(String productName) {
        super(MessageFormat.format(MESSAGE, productName));
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }
}
