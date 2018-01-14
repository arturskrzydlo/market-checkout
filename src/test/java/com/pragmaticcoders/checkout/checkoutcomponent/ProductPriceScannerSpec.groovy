package com.pragmaticcoders.checkout.checkoutcomponent

import spock.lang.Specification

import java.lang.Void as Should

class ProductPriceScannerSpec extends Specification {

    def sampleProductToCheck = createSampleProduct()
    def productRepository = Mock(ProductRepository)
    def producteService = new ProductServiceImpl(productRepository)


    Should "Return actual price of given product"() {

        given: "name of existing product"
            def productName = sampleProductToCheck.getName()

        and: "this product is correctly returned from database"
            productRepository.find() >> createSampleProduct()

        when: "trying to find actual price for product"
            def resultPrice = producteService.findPrice(productName)

        then:
            resultPrice == sampleProductToCheck.getPrice()


    }


    def createSampleProduct() {

        Product product = new Product()
        product.setName("toothbrush")
        product.setPrice(5.0)
        product.amountInStorage(10)


        return product
    }
}
