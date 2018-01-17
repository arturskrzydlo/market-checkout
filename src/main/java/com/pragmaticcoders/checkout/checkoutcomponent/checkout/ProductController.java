package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products") class ProductController {

    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{productName}")
    public Map<String, Double> scanProduct(@PathVariable String productName) throws ProductNotFoundException {
        return Collections.singletonMap("price", productService.findActualPriceForProduct(productName));
    }

    @GetMapping(value = "/{productName}", params = {"quantity"})
    public Map<String, Double> getProductPriceWithDiscounts(@PathVariable String productName,
            @RequestParam Integer quantity)
            throws ProductNotFoundException {

        return Collections.singletonMap("price", productService.countProductPriceWithPromotions(productName, quantity));
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
