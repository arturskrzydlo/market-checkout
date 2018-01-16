package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.beans.factory.annotation.Autowired;

class PromoServiceImpl implements PromoService {

    private PromoRepository promoRepository;
    private ProductRepository productRepository;

    @Autowired
    public PromoServiceImpl(PromoRepository promoRepository, ProductRepository productRepository) {
        this.promoRepository = promoRepository;
        this.productRepository = productRepository;
    }

    @Override public Promo createMultiPricedPromo(String productName, Integer unitAmount, Double specialPrice)
            throws ProductNotFoundException {

        Product product = productRepository.findByName(productName);
        if (product == null) {
            throw new ProductNotFoundException(productName);
        }
        Promo promo = new Promo();
        promo.addProduct(product);
        promo.setSpecialPrice(specialPrice);
        promo.setUnitAmount(unitAmount);

        promoRepository.save(promo);
        return promo;
    }
}
