package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import spock.lang.Specification

import java.lang.Void as Should

class ProductPriceScannerSpec extends Specification {

    def sampleProductToCheck = createSampleProduct()
    def productRepository = Mock(ProductRepository)
    def producteService = new ProductServiceImpl(productRepository)
    def promo1
    def promo2
    def promo3


    Should "Return actual price of given product"() {

        given: "name of existing product"
            def productName = sampleProductToCheck.getName()

        and: "this product is correctly returned from database"
            productRepository.findByName(productName) >> createSampleProduct()

        when: "trying to find actual price for product"
            def resultPrice = producteService.findActualPriceForProduct(productName)
        then:
            resultPrice == sampleProductToCheck.getPrice()

    }

    Should "throw ProductNotFoundException when given name is not name of existing product"() {

        given: "name of not existing product"
            def nonExistingProductName = "nonExistingProductName"

        and: "repository can't find such a product and return null instead"
            productRepository.findByName(nonExistingProductName) >> null

        when: "trying to find actual price for product"
            producteService.findActualPriceForProduct(nonExistingProductName)
        then:
            ProductNotFoundException exception = thrown()
            exception.message == "Product with name " + nonExistingProductName + " does not exists"

    }

    Should "get real product price after promotions application"() {
        given: "product name"
            def productName = sampleProductToCheck.getName()
        and: "quantity in order"
            def quantity = 10
        when:
            finalPrice = producteService.countProductPriceWithPromotions(productName, quantity)
        then:
            1 * productRepository.findByName(productName) >> sampleProductToCheck
        where:
            quantity | finalPrice
            0        | 0
            1        | sampleProductToCheck.getPrice()
            5        | promo3.getPrice()
            7        | promo3.getPrice() + 2 * sampleProductToCheck
            10       | promo1.getPrice()
            11       | promo1.getPrice + sampleProductToCheck
            15       | promo1.getPrice + promo3.getPrice
            20       | promo2.getPrice()
            30       | promo2.getPrice + promo1.getPrice() //always get more beneficial to customer promotion
            100      | 5 * promo2.getPrice
            115      | 5 * promo2.getPrice + promo1.getPrice() + promo3.getPrice

    }


    def createManyPromosForSampleProduct() {

        Product sampleProduct = createSampleProduct()
        promo1 = new Promo()
        promo1.setUnitAmount(10)
        promo1.setSpecialPrice(30)
        promo1.addProduct(sampleProduct)

        promo2 = new Promo()
        promo2.setUnitAmount(20)
        promo2.setSpecialPrice(35)
        promo2.addProduct(sampleProduct)

        promo3 = new Promo()
        promo3.setUnitAmount(5)
        promo3.setSpecialPrice(20)
        promo3.addProduct(sampleProduct)

        List<Promo> promos = [promo1, promo2, promo3]
        return promos

    }

    def createSampleProduct() {

        Product product = new Product()
        product.setName("toothbrush")
        product.setPrice(5.0)
        product.setAmountInStorage(10)

        return product
    }
}
