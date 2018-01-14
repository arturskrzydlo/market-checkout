package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import spock.lang.Specification

import java.lang.Void as Should

class PromoServiceSpec extends Specification {

    def sampleProductToCheck = createSampleProduct()
    def samplePromo = createSamplePromo()
    def productRepository = Mock(ProductRepository)
    def promoRepository = Mock(Promo)
    def promoService = new PromoServiceImpl(productRepository)


    Should "sucessfully create multi-priced promo for specified amount of units"() {

        given: "sample product on which promo should be applied"
        and: "amount of product units, until where promo will be applied"
            def unitsAmount = samplePromo.getUnits()
        and: "price for product when reach required units number"
            def specialPrice = samplePromo.getSpecialPrice()
        and: "product on which promo will be applied exists"
            productRepository.findByName(sampleProductToCheck.getName()) >> sampleProductToCheck

        when: "creating promo"
            def resultPromo = promoService.createMultiPricedPromo(sampleProductToCheck.getName(), unitsAmount, specialPrice)
        then:
            1 * promoRepository.save(_) >> {
                Promo promo ->
                    assert promo.getUnits == unitsAmount
                    assert promo.getSpecialPrice == specialPrice
                    asssert promo.getProducts.contains(sampleProductToCheck)
                    return samplePromo
            }
    }

    def createSamplePromo() {
        Promo promo = new Promo()
        promo.setUnits(5)
        promo.setSpecialPrice(20)
        promo.addProduct(createSampleProduct())

        return promo
    }

    def createSampleProduct() {

        Product product = new Product()
        product.setName("toothbrush")
        product.setPrice(5.0)
        product.setAmountInStorage(10)
        product.setId(1)

        return product
    }
}
