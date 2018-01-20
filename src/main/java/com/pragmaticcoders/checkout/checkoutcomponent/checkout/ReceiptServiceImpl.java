package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service class ReceiptServiceImpl implements ReceiptService {

    private ProductRepository productRepository;
    private ReceiptRepository receiptRepository;

    @Autowired
    public ReceiptServiceImpl(ProductRepository productRepository, ReceiptRepository receiptRepository) {
        this.productRepository = productRepository;
        this.receiptRepository = receiptRepository;
    }

    @Override
    @Transactional
    public Double addProductToReceipt(ScannedProductDTO product, Integer receiptId) {
        Product productFromRepo = productRepository.findByName(product.getProductName());

        Receipt receipt = updateReceipt(productFromRepo, receiptId, product.getQuantity());
        receiptRepository.save(receipt);
        return productFromRepo.getPrice() * product.getQuantity();
    }

    private Receipt updateReceipt(Product product, Integer receiptId, Integer quantity) {

        Receipt receipt = receiptRepository.findOne(receiptId);
        Optional<ReceiptItem> receiptItemForProduct = receipt.getItems().stream()
                .filter(receiptItem -> receiptItem.getProduct().equals(product) && receiptItem.getReceipt()
                        .equals(receipt))
                .findFirst();

        if (receiptItemForProduct.isPresent()) {
            receiptItemForProduct.get().setQuantity(receiptItemForProduct.get().getQuantity() + quantity);
        } else {
            receipt.addItem(createReceiptItem(product, quantity, receipt));
        }

        return receipt;

    }

    private ReceiptItem createReceiptItem(Product product, Integer quantity, Receipt receipt) {

        ReceiptItem receiptItem = new ReceiptItem();
        receiptItem.setProduct(product);
        receiptItem.setReceipt(receipt);
        receiptItem.setQuantity(quantity);

        return receiptItem;
    }
}
