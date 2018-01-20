package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import java.text.MessageFormat;

class ResourceNotFoundException extends Exception {

    private static final String MESSAGE = "{0} with identity {1} does not exists";
    String identity;

    public ResourceNotFoundException(String identity, String resourceName) {
        super(MessageFormat.format(MESSAGE, resourceName, identity));
        this.identity = identity;
    }

    public String getIdentity() {
        return identity;
    }
}
