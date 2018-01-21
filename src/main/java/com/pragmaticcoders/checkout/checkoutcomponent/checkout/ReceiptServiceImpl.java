package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service class ReceiptServiceImpl implements ReceiptService {

    private ProductRepository productRepository;
    private ReceiptRepository receiptRepository;
    private ProductService productService;

    @Autowired
    public ReceiptServiceImpl(ProductRepository productRepository, ReceiptRepository receiptRepository,
            ProductService productService) {
        this.productRepository = productRepository;
        this.receiptRepository = receiptRepository;
        this.productService = productService;
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

    @Override
    public Receipt createNewReceipt() {

        Receipt receipt = new Receipt();
        return receiptRepository.save(receipt);
    }

    @Override
    public Receipt produceReceiptWithPayment(Integer receiptId) throws ReceiptNotFoundException {

        Receipt receipt = getReceiptById(receiptId);
        calculatePayment(receipt);
        return receipt;
    }

    private void calculatePayment(Receipt receipt) {

        double payment = 0.0;
        for (ReceiptItem receiptItem : receipt.getItems()) {
            payment = +productService
                    .countProductPriceWithPromotions(receiptItem.getProduct(), receiptItem.getQuantity());
        }
        receipt.setPayment(payment);
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

    private double applyAsDouble(ReceiptItem item) throws ProductNotFoundException {
        return productService
                .countProductPriceWithPromotions(item.getProduct().getName(), item.getQuantity());
    }
}
