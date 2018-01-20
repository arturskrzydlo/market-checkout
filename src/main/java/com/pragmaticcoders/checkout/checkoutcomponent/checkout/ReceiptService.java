package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

interface ReceiptService {

    Double addProductToReceipt(ScannedProductDTO product, Integer receiptId)
            throws ProductNotFoundException, ReceiptNotFoundException;

    Receipt createNewReceipt();
}
