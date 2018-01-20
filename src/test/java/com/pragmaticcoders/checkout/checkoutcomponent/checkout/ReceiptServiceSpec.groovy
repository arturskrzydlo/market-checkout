package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import spock.lang.Specification

import java.lang.Void as Should

class ReceiptServiceSpec extends Specification {

    def productRepository = Mock(ProductRepository)
    def receiptRepository = Mock(ReceiptRepository)
    def cartService = new ReceiptServiceImpl(productRepository)

    Should "get product by it's name and return it's price for existing product name"() {
        given: "scanned product with name and quantity"
            def scannedProduct = createScannedProduct()
        and:
            def productFound = createProduct()
        when: "add product has been called"
            def result = cartService.addProductToReceipt(scannedProduct)
        then:
            1 * productRepository.findByName(scannedProduct.getProductName()) >> productFound
        and:
            result == productFound.getPrice() * scannedProduct.quantity

    }

    Should "add cart to cart datatable when cart has been created yet"() {
        given: "scanned product with name and quantity"
            def scannedProduct = createScannedProduct()
        and:
            def productFound = createProduct()
        and:
            def receipt = new Receipt()
        when: "add product has been called"
            def result = cartService.addProductToReceipt(scannedProduct)
        then:
            1 * productRepository.findByName(scannedProduct.getProductName()) >> productFound
        and:
            1 * receiptRepository.save(_) >> {
                Receipt receipt1 ->
                    assert receipt1.getReceiptItems().size() == 1

            }

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
