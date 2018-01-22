package com.pragmaticcoders.checkout.checkoutcomponent.general;

import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductNotFoundException;
import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductService;
import com.pragmaticcoders.checkout.checkoutcomponent.promo.PromoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Profile("application")
public class CheckoutBootstrap implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired PromoService promoService;
    @Autowired ProductService productService;

    private final String toothbrush = "toothbrush";
    private final String keyboard = "keyboard";
    private final String headphones = "headphones";

    @Override public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        createProduct();
        try {
            createPromos();
        } catch (ProductNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createPromos() throws ProductNotFoundException {

        promoService.createMultiPricedPromo(toothbrush,5,10.0);
        promoService.createMultiPricedPromo(keyboard,2,35.0);
        promoService.createMultiPricedPromo(headphones,2,230.0);
    }

    private void createProduct() {

        productService.createProduct(toothbrush, 5.0);
        productService.createProduct(keyboard,20.0);
        productService.createProduct(headphones,150.0);
    }
}
