package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RequestMapping("/receipt")
@RestController class ReceiptController {

    private ReceiptService receiptService;

    @PutMapping("/{receiptId}")
    public Map<String, Double> scanProduct(@RequestBody ScannedProductDTO scannedProductDTO,
            @PathVariable Integer receiptId) {
        return Collections.singletonMap("price", receiptService.addProductToReceipt(scannedProductDTO, receiptId));
    }

    @Autowired
    public void setReceiptService(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }
}
