package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import spock.lang.Specification

import java.lang.Void as Should

class CartServiceSpec extends Specification {

    def productRepository = Mock(ProductRepository)
    def cartService = new CartServiceImpl()

    Should "get product by it's name, add to cart and return it's price for existing product name"() {
        given: "scanned product with name and quantity"
            def scannedProduct = createScannedProduct()
        and:
            def productFound = createProduct()
        when: "add product has been called"
            def result = cartService.addProductToCard(scannedProduct)
        then:
            1 * productRepository.findByName(scannedProduct.getProductName()) >> productFound
        and:
            result == productFound.getPrice()

    }

    def createProduct() {

        Product product = new Product()
        product.setId(1)
        product.setName("toothbrush")
        product.setPrice(5.0)

        return product

    }

    def createScannedProduct() {

        ScannedProductDTO scannedProductDTO = new ScannedProductDTO()
        scannedProductDTO.setProductName("toothbrush")
        scannedProductDTO.setQuantity(3)

        return scannedProductDTO
    }
}
