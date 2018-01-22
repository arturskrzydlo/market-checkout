package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RequestMapping("/receipt")
@RestController class ReceiptController {

    private ReceiptService receiptService;

    @Autowired
    private ModelMapper modelMapper;

    @PatchMapping("/{receiptId}/product")
    public Map<String, Double> scanProduct(@RequestBody ScannedProductDTO scannedProductDTO,
            @PathVariable Integer receiptId)
            throws ProductNotFoundException, ReceiptNotFoundException, ReceiptClosedException {
        return Collections.singletonMap("price", receiptService.addProductToReceipt(scannedProductDTO, receiptId));
    }

    @PatchMapping("/{receiptId}")
    public ResponseEntity<?> updateReceiptState(@RequestBody ReceiptStateDTO receiptStateDTO,
            @PathVariable Integer receiptId) throws ReceiptNotFoundException {

        receiptService.updateReceiptState(receiptStateDTO.isOpened(), receiptId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceiptDTO createReceipt() {
        return convertToDTO(receiptService.createNewReceipt());
    }

    @GetMapping("/{receiptId}")
    public ReceiptDTO getReceiptWithPayment(@PathVariable Integer receiptId) throws ReceiptNotFoundException {
        return convertToDTO(receiptService.produceReceiptWithPayment(receiptId));
    }

    @Autowired
    public void setReceiptService(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    private ReceiptDTO convertToDTO(Receipt receipt) {

        ReceiptDTO receiptDTO = modelMapper.map(receipt, ReceiptDTO.class);
        return receiptDTO;
    }

    private Receipt oonvertToEntity(ReceiptDTO receiptDTO) {

        Receipt receipt = modelMapper.map(receiptDTO, Receipt.class);
        return receipt;
    }
}
