package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import java.text.MessageFormat;

public class ReceiptClosedException extends Exception {

    private static final String MESSAGE = "Receipt with id {0} is already closed. It's not possible to modify this receipt anymore";
    Integer receiptId;

    public ReceiptClosedException(Integer receiptId) {
        super(MessageFormat.format(MESSAGE, receiptId));
        this.receiptId = receiptId;
    }

}
