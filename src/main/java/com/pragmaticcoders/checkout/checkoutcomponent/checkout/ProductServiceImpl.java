package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override public Double findActualPriceForProduct(String productName) throws ProductNotFoundException {
        Optional<Product> product = Optional.ofNullable(productRepository.findByName(productName));
        return product
                .map(product1 -> product1.getPrice())
                .orElseThrow(() -> new ProductNotFoundException(productName));
    }

    @Override public Double countProductPriceWithPromotions(String productName, int quantity)
            throws ProductNotFoundException {

        if (quantity < 1) {
            return 0.0;
        }

        Optional<Product> product = Optional.ofNullable(productRepository.findByName(productName));
        return countProductPriceWithPromotions(product.orElseThrow(() -> new ProductNotFoundException(productName)),
                quantity, Collections.emptySet());
    }

    @Override
    public Double countProductPriceWithPromotions(Product product, int quantity,
            Set<ReceiptItem> allProductsInReceipt) {
        if (quantity < 1 || product == null) {
            return 0.0;
        }
        return applyPromotionsOnProductPrice(product, quantity, allProductsInReceipt);
    }

    @Override public List<Product> getAllProducts() {
        return StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    //TODO can shorten method argumentts to two - refactor !
    private Double applyPromotionsOnProductPrice(Product product, int quantity,
            Set<ReceiptItem> allProductsInReceipt) {

        double bestCombinedPromoPrice = chooseMostBeneficialCombinedPromo(product, quantity, allProductsInReceipt);
        List<Promo> promotions = chooseMostBeneficialMultiPricedPromotion(product, quantity);
        double bestMultiPricePromoPrice = countMultiPricedPromotion(promotions, product, quantity);

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

    private static double chooseMostBeneficialCombinedPromo(Product product, int quantity,
            Set<ReceiptItem> receiptItems) {

        Set<Promo> combinedPromos = product.getPromos().stream()
                .filter(promo -> promo.getType().equals(PromoType.COMBINED))
                .collect(Collectors.toSet());

        double minPrice = Integer.MAX_VALUE;

        for (ReceiptItem receiptItem : receiptItems) {

            Optional<Promo> optionalPromo = combinedPromos.stream()
                    .filter(promo -> promo.getProducts().contains(receiptItem.getProduct()))
                    .findFirst();

            if (optionalPromo.isPresent()) {
                double promoPrice =
                        Math.min(quantity, receiptItem.getQuantity()) * optionalPromo.get().getSpecialPrice();
                if (promoPrice < minPrice) {
                    minPrice = promoPrice;
                }
            }
        }

        return minPrice;
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
}
