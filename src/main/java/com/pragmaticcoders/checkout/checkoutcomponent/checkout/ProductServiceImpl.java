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
        return product
                .map(prod -> applyPromotionsOnProductPrice(prod, quantity))
                .orElseThrow(() -> new ProductNotFoundException(productName));
    }

    @Override public List<Product> getAllProducts() {
        return StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    private static Double applyPromotionsOnProductPrice(Product product, int quantity) {

        List<Promo> promotions = chooseMostBeneficialMultiPricedPromotion(product.getPromos(), quantity);

        double promotionsPrice = promotions.stream()
                .mapToDouble(promo -> promo.getSpecialPrice())
                .sum();

        int promoQuantity = promotions.stream()
                .mapToInt(promo -> promo.getUnitAmount())
                .sum();

        return promotionsPrice + (quantity - promoQuantity) * product.getPrice();
    }

    private static List<Promo> chooseMostBeneficialMultiPricedPromotion(Set<Promo> allMultiPricedPromotions,
            int quantity) {

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
