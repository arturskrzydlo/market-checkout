package com.pragmaticcoders.checkout.checkoutcomponent.promo

import com.pragmaticcoders.checkout.checkoutcomponent.products.Product
import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductNotFoundException
import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductService
import spock.lang.Specification

import java.lang.Void as Should

class PromoServiceSpec extends Specification {

    def sampleProductToCheck = createSampleProduct()
    def samplePromo = createSamplePromo()
    def promoRepository = Mock(PromoRepository)
    def productService = Mock(ProductService)
    def promoService = new PromoServiceImpl(promoRepository, productService)


    Should "sucessfully create multi-priced promo for specified amount of units"() {

        given: "sample product on which promo should be applied"
        and: "amount of product units, until where promo will be applied"
            def unitsAmount = samplePromo.getUnitAmount()
        and: "price for product when reach required units number"
            def specialPrice = samplePromo.getSpecialPrice()
        and: "product on which promo will be applied exists"
            productService.findProductByName(sampleProductToCheck.getName()) >> sampleProductToCheck

        when: "creating promo"
            def resultPromo = promoService.createMultiPricedPromo(sampleProductToCheck.getName(), unitsAmount, specialPrice)
        then:
            1 * promoRepository.save(_) >> {
                Promo promo ->
                    assert promo.getUnitAmount() == unitsAmount
                    assert promo.getSpecialPrice() == specialPrice
                    assert promo.getProducts().contains(sampleProductToCheck)
                    return samplePromo
            }
            1 * productService.findProductByName(sampleProductToCheck.getName()) >> sampleProductToCheck


    }

    Should "throw ProductNotFoundException when product name doesn't exists for creating new promo"() {
        given: "sample product name which doesn't exists"
            def productName = "notExistingProductName"
        and: "amount of product units, until where promo will be applied"
            def unitsAmount = samplePromo.getUnitAmount()
        and: "price for product when reach required units number"
            def specialPrice = samplePromo.getSpecialPrice()
        when:
            promoService.createMultiPricedPromo(productName, unitsAmount, specialPrice)
        then:
            ProductNotFoundException exception = thrown()
            exception.message == "Product with identity " + productName + " does not exists"
        and:
            1 * productService.findProductByName(productName) >> {
                throw new ProductNotFoundException(productName)
            }
        and:
            0 * promoRepository.save(_)
    }

    Should "return all promotions for given product"() {
        given: "product name"
            def productName = sampleProductToCheck.getName()
        and: "there exists some promotions for this product"
            def manyPromosForSampleProduct = createManyPromosForSampleProduct()
        when:
            def allResults = promoService.getAllPromotionsForProduct(productName)
        then:
            !allResults.isEmpty()
            allResults.size() == manyPromosForSampleProduct.size()
            1 * productService.findProductByName(productName) >> sampleProductToCheck
            1 * promoRepository.findByProducts_Name(productName) >> manyPromosForSampleProduct
    }

    Should "throw ProductNotFoundException when product name doesn't exists when getting all promotions"() {
        given: "sample product name which doesn't exists"
            def productName = "notExistingProductName"
        when:
            promoService.getAllPromotionsForProduct(productName)
        then:
            ProductNotFoundException exception = thrown()
            exception.message == "Product with identity " + productName + " does not exists"
        and:
            1 * productService.findProductByName(productName) >> {
                throw new ProductNotFoundException(productName)
            }
    }


    def createSamplePromo() {
        Promo promo = new Promo()
        promo.setUnitAmount(5)
        promo.setSpecialPrice(20)
        promo.addProduct(createSampleProduct())

        return promo
    }

    def createManyPromosForSampleProduct() {

        Product sampleProduct = createSampleProduct()
        Promo promo1 = new Promo()
        promo1.setUnitAmount(10)
        promo1.setSpecialPrice(30)
        promo1.addProduct(sampleProduct)

        Promo promo2 = new Promo()
        promo2.setUnitAmount(20)
        promo2.setSpecialPrice(35)
        promo2.addProduct(sampleProduct)

        Promo promo3 = new Promo()
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
        product.setId(1)

        return product
    }
}
