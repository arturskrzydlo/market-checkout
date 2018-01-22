package com.pragmaticcoders.checkout.checkoutcomponent.products;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products") class ProductController {

    private ProductService productService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{productName}")
    public Map<String, Double> scanProduct(@PathVariable String productName) throws ProductNotFoundException {
        return Collections.singletonMap("price", productService.findActualPriceForProduct(productName));
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    private ProductDTO convertToDTO(Product receipt) {
        ProductDTO productDTO = modelMapper.map(receipt, ProductDTO.class);
        return productDTO;
    }

}
