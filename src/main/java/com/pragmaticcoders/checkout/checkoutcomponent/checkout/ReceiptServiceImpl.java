package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

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
        receiptRepository.save(receipt);
        return receipt;
    }

    private void calculatePayment(Receipt receipt) {

        double payment = receipt.getItems().stream()
                .mapToDouble(receiptItem -> countProductPriceWithPromotions(receiptItem, receipt.getItems()))
                .sum();

        receipt.setPayment(payment);

    }

    private Double countProductPriceWithPromotions(ReceiptItem receiptItem, Set<ReceiptItem> allItems) {
        if (receiptItem.getQuantity() < 1 || receiptItem.getProduct() == null) {
            return 0.0;
        }
        double priceForReceiptItem = applyPromotionsOnProductPrice(receiptItem, allItems);
        receiptItem.setPrice(priceForReceiptItem);
        return priceForReceiptItem;
    }

    private Double applyPromotionsOnProductPrice(ReceiptItem receiptItem, Set<ReceiptItem> allItems) {

        double bestCombinedPromoPrice = getMostBeneficialCombinePromoPrice(receiptItem.getProduct(),
                receiptItem.getQuantity(),
                allItems);
        List<Promo> promotions = chooseMostBeneficialMultiPricedPromotion(receiptItem.getProduct(),
                receiptItem.getQuantity());
        double bestMultiPricePromoPrice = countMultiPricedPromotion(promotions, receiptItem.getProduct(),
                receiptItem.getQuantity());

        return bestCombinedPromoPrice <= bestMultiPricePromoPrice ? bestCombinedPromoPrice : bestMultiPricePromoPrice;

    }

    private double countMultiPricedPromotion(List<Promo> promotions, Product product, int quantity) {

        double promotionsPrice = promotions.stream()
                .mapToDouble(promo -> promo.getSpecialPrice())
                .sum();

        int promoQuantity = promotions.stream()
                .mapToInt(promo -> promo.getUnitAmount())
                .sum();

        return promotionsPrice + (quantity - promoQuantity) * product.getPrice();
    }

    private Set<Product> getProductsWithWhichCalculatedProductCombinesWithInPromotion(Product product) {
        return product.getPromos().stream()
                .filter(promo -> promo.getType().equals(PromoType.COMBINED))
                .map(promo -> promo.getProducts().stream()
                        .filter(product1 -> !product1.equals(product)).collect(Collectors.toList()))
                .flatMap(productsList -> productsList.stream())
                .collect(Collectors.toSet());
    }

    private Optional<Promo> findPromotionForProduct(Set<Promo> promos, Product product) {
        return promos.stream().filter(promo -> promo.getProducts().contains(product)).findFirst();
    }

    private Optional<ReceiptItem> findReceiptForProduct(Set<ReceiptItem> receiptItems, Product product) {
        return receiptItems.stream()
                .filter(receiptItem -> receiptItem.getProduct().equals(product))
                .findAny();
    }

    private double getMostBeneficialCombinePromoPrice(Product product, int quantity,
            Set<ReceiptItem> receiptItems) {

        Set<Product> products = getProductsWithWhichCalculatedProductCombinesWithInPromotion(product);

        double minPrice = Integer.MAX_VALUE;

        for (Product productToCombineWith : products) {

            Optional<ReceiptItem> receiptForProduct = findReceiptForProduct(receiptItems, productToCombineWith);
            if (receiptForProduct.isPresent()) {

                Optional<Promo> optionalPromo = findPromotionForProduct(product.getPromos(), productToCombineWith);
                if (optionalPromo.isPresent()) {

                    Promo promo = optionalPromo.get();
                    //reduce price only of more expensive product
                    Optional<Product> productWithHigherPrice = promo
                            .getProducts()
                            .stream()
                            .max(Comparator.comparing(product1 -> product1.getPrice()));

                    Optional<Product> productWithLowePrice = promo
                            .getProducts()
                            .stream()
                            .min(Comparator.comparing(product1 -> product1.getPrice()));

                    if (product.equals(productWithHigherPrice.orElse(product))) {

                        int minQuantity = Math.min(quantity, receiptForProduct.get().getQuantity());
                        // is special price minus price of lower price product. Promotion is applied only on one product, so
                        // special price must be substracted by lower price products
                        double lowerPrice = 0.0;
                        if (productWithLowePrice.isPresent()) {
                            lowerPrice = productWithLowePrice.get().getPrice();
                        }
                        double specialPrice = promo.getSpecialPrice() - lowerPrice;
                        // promo price consist of units on which promotion applies (for instance 2 units of product)
                        // multiplied by special price and then the rest of the product units cos normal price
                        double promoPrice = minQuantity * specialPrice + (quantity - minQuantity) * product.getPrice();
                        if (promoPrice < minPrice) {
                            minPrice = promoPrice;
                        }
                    }
                }
            }

        }

        return minPrice == Integer.MAX_VALUE ? product.getPrice() : minPrice;
    }

    private static List<Promo> chooseMostBeneficialMultiPricedPromotion(Product product,
            int quantity) {

        Set<Promo> allMultiPricedPromotions = product.getPromos().stream()
                .filter(promo -> promo.getType().equals(PromoType.MULTIPRICE)).collect(
                        Collectors.toSet());

        HashMap<Integer, List<Promo>> promosForGivenQuantity = new HashMap<>();

        //TODO: SIMPLIFY this algorithm
        for (int q = 1; q <= quantity; q++) {
            for (Promo promo : allMultiPricedPromotions) {
                int promotionQuantity = promo.getUnitAmount();
                if (q >= promotionQuantity) {

                    List<Promo> promosForGivenValue = new ArrayList<>(
                            promosForGivenQuantity.getOrDefault(q, new ArrayList<>()));
                    if (promosForGivenValue.isEmpty()) {
                        promosForGivenValue = new ArrayList<>(
                                promosForGivenQuantity.getOrDefault(q - promotionQuantity, new ArrayList<>()));
                        promosForGivenValue.add(promo);
                    } else {

                        if (q >= promotionQuantity) {

                            List promotionsForPreviousCoins = new ArrayList<>(
                                    promosForGivenQuantity.getOrDefault(q - promotionQuantity, new ArrayList<>()));

                            if (promosForGivenValue.size() > promotionsForPreviousCoins.size()) {
                                promosForGivenValue = new ArrayList<>(
                                        promosForGivenQuantity.getOrDefault(q - promotionQuantity, new ArrayList<>()));
                                promosForGivenValue.add(promo);
                            }
                        }
                    }
                    promosForGivenQuantity.put(q, promosForGivenValue);
                }
            }
        }
        return promosForGivenQuantity.getOrDefault(quantity, new ArrayList<>());
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
