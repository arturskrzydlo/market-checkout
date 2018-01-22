package com.pragmaticcoders.checkout.checkoutcomponent.promo;

import com.pragmaticcoders.checkout.checkoutcomponent.products.Product;
import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductNotFoundException;
import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
class PromoServiceImpl implements PromoService {

    private PromoRepository promoRepository;
    private ProductService productService;

    @Autowired
    public PromoServiceImpl(PromoRepository promoRepository, ProductService productService) {
        this.promoRepository = promoRepository;
        this.productService = productService;
    }

    @Transactional
    @Override public Promo createMultiPricedPromo(String productName, Integer unitAmount, Double specialPrice)
            throws ProductNotFoundException {

        Product product = productService.findProductByName(productName);

        Promo promo = new Promo();
        promo.addProduct(product);
        promo.setSpecialPrice(specialPrice);
        promo.setUnitAmount(unitAmount);
        promo.setType(PromoType.MULTIPRICE);

        promoRepository.save(promo);
        return promo;
    }

    @Override public List<Promo> getAllPromotionsForProduct(String productName) throws ProductNotFoundException {
        productService.findProductByName(productName);
        List<Promo> promos = promoRepository.findByProducts_Name(productName);
        return promos;
    }
}
