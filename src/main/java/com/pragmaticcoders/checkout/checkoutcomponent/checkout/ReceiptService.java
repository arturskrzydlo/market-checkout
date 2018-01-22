package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductNotFoundException;

interface ReceiptService {

    Double addProductToReceipt(ScannedProductDTO product, Integer receiptId)
            throws ProductNotFoundException, ReceiptNotFoundException;

    Receipt createNewReceipt();

    Receipt produceReceiptWithPayment(Integer receiptId) throws ReceiptNotFoundException;
}
