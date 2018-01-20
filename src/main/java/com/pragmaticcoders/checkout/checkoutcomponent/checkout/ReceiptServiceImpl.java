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
    public Double addProductToReceipt(ScannedProductDTO product, Integer receiptId)
            throws ProductNotFoundException, ReceiptNotFoundException {
        Product productFromRepo = getProductByProductName(product.getProductName());

        Receipt receipt = updateReceipt(productFromRepo, receiptId, product.getQuantity());
        receiptRepository.save(receipt);
        return productFromRepo.getPrice() * product.getQuantity();
    }

    private Receipt updateReceipt(Product product, Integer receiptId, Integer quantity)
            throws ReceiptNotFoundException {

        Receipt receipt = getReceiptById(receiptId);
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

    private Receipt getReceiptById(Integer receiptId) throws ReceiptNotFoundException {
        Receipt receipt = receiptRepository.findOne(receiptId);
        if (receipt == null) {
            throw new ReceiptNotFoundException(receiptId);
        }

        return receipt;
    }

    //TODO: move it to product service - it's redundant with promo service impl
    private Product getProductByProductName(String productName) throws ProductNotFoundException {

        Product product = productRepository.findByName(productName);
        if (product == null) {
            throw new ProductNotFoundException(productName);
        }

        return product;
    }
}
