package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

public interface ReceiptService {

    Double addProductToReceipt(ScannedProductDTO product, Integer receiptId);
}
