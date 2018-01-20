package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

class ReceiptNotFoundException extends ResourceNotFoundException {

    private static final String RESOURCE_NAME = "Receipt";

    public ReceiptNotFoundException(Integer receiptId) {
        super(receiptId.toString(), RESOURCE_NAME);
    }

}
