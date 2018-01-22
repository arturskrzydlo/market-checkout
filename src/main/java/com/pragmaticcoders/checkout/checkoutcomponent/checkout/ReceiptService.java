package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductNotFoundException;

interface ReceiptService {

    Double addProductToReceipt(ScannedProductDTO product, Integer receiptId)
            throws ProductNotFoundException, ReceiptNotFoundException, ReceiptClosedException;

    Receipt createNewReceipt();

    Receipt produceReceiptWithPayment(Integer receiptId) throws ReceiptNotFoundException;

    void updateReceiptState(boolean opened, Integer receiptId) throws ReceiptNotFoundException;
}
