package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.pragmaticcoders.checkout.checkoutcomponent.general.errors.ResourceNotFoundException;

class ReceiptNotFoundException extends ResourceNotFoundException {

    private static final String RESOURCE_NAME = "Receipt";

    public ReceiptNotFoundException(Integer receiptId) {
        super(receiptId.toString(), RESOURCE_NAME);
    }

}
