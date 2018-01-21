package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.Void as Should

class ProductPriceScannerSpec extends Specification {

    def sampleProductToCheck
    def productRepository = Mock(ProductRepository)
    def producteService = new ProductServiceImpl(productRepository)

    @Shared
    def promo1
    @Shared
    def promo2
    @Shared
    def promo3
    @Shared
    def productPrice = createSampleProduct().getPrice()

    //TODO fix to be possible to run just one test
    def setup() {
        sampleProductToCheck = createSampleProduct()
        createManyPromosForSampleProduct()
    }


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
            exception.message == "Product with identity " + nonExistingProductName + " does not exists"

    }

    @Unroll
    Should "get real product price after promotions application for #quantity unit of product"() {
        given: "product name"
            def productName = sampleProductToCheck.getName()
        and: "product with 3 multipriced promos"
            sampleProductToCheck.addPromo(promo1)
            sampleProductToCheck.addPromo(promo2)
            sampleProductToCheck.addPromo(promo3)
        when:
            def result = producteService.countProductPriceWithPromotions(productName, quantity)
        then:
            1 * productRepository.findByName(productName) >> sampleProductToCheck
            result == finalPrice
        where:
            quantity | finalPrice
            1        | productPrice
            5        | promo3.getSpecialPrice()
            7        | promo3.getSpecialPrice() + 2 * productPrice
            10       | promo1.getSpecialPrice()
            11       | promo1.getSpecialPrice() + productPrice
            15       | promo1.getSpecialPrice() + promo3.getSpecialPrice()
            20       | promo2.getSpecialPrice()
            30       | promo2.getSpecialPrice() + promo1.getSpecialPrice()
            100      | 5 * promo2.getSpecialPrice()
            115      | 5 * promo2.getSpecialPrice() + promo1.getSpecialPrice() + promo3.getSpecialPrice()

    }

    Should "return 0 when calling to calculate product price with null product"() {
        given: "null product"
            Product product = null
        when:
            def result = producteService.countProductPriceWithPromotions(product as Product, 1)
        then:
            result == 0.0
    }


    Should "return 0 price, when quantity is less then 1"() {
        given: "product name"
            def productName = sampleProductToCheck.getName()
        and: "product with 3 multipriced promos"
            sampleProductToCheck.addPromo(promo1)
            sampleProductToCheck.addPromo(promo2)
            sampleProductToCheck.addPromo(promo3)
        when:
            def result = producteService.countProductPriceWithPromotions(productName, quantity)
        then:
            0 * productRepository.findByName(productName) >> sampleProductToCheck
            result == finalPrice
        where:
            quantity | finalPrice
            -1       | 0
            0        | 0
    }

    Should "throw ProductNotFoundException when product name does not exists"() {

        given: "name of not existing product"
            def nonExistingProductName = "nonExistingProductName"

        and: "repository can't find such a product and return null instead"
            productRepository.findByName(nonExistingProductName) >> null

        when: "trying to find actual price for product"
            producteService.countProductPriceWithPromotions(nonExistingProductName, 1)
        then:
            ProductNotFoundException exception = thrown()
            exception.message == "Product with identity " + nonExistingProductName + " does not exists"
    }

    Should "return combined promotion when no other promotion exists"() {

        given: "one combined promo for a sample product"
            def combinedPromo = creaetCombinedPromoForSampleProduct()
        and:
            def listOfOtherProducts = [combinedPromo.getProducts()[1]]
        when:
            def result = producteService.countProductPriceWithPromotions(combinedPromo.getProducts()[0], 2, listOfOtherProducts)
        then:
            result == combinedPromo.getSpecialPrice()


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

    def creaetCombinedPromoForSampleProduct() {

        Product product1 = createSampleProduct()
        Promo combinedPromo = new Promo()
        combinedProm.setType(PromoType.COMBINED)
        combinedPromo.addProduct(product1)

        Product product2 = new Product()
        product2.setPrice(20.0)
        product2.setName("headphones")

        combinedPromo.addProduct(product2)
        combinedPromo.setSpecialPrice(product2.getPrice() + product1.getPrice() - 10.0)

        return combinedPromo
    }

    def createSampleProduct() {

        Product product = new Product()
        product.setName("toothbrush")
        product.setPrice(5.0)
        product.setAmountInStorage(10)

        return product
    }
}
