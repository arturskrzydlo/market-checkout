package com.pragmaticcoders.checkout.checkoutcomponent.products

import com.pragmaticcoders.checkout.checkoutcomponent.checkout.ReceiptItem
import com.pragmaticcoders.checkout.checkoutcomponent.promo.Promo
import com.pragmaticcoders.checkout.checkoutcomponent.promo.PromoType
import spock.lang.Shared
import spock.lang.Specification

import java.lang.Void as Should

class ProductServiceSpec extends Specification {

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


    Should "create new product with assigned it to it when price and name are specified"() {
        given: "name and price of the product"
            def price = 5.0
            def name = "toothbrush"
        when:
            def createdProduct = producteService.createProduct()
        then: "created product has correct price, name and not empty id"
            createdProduct.price == price
            createdProduct.name == name
            createdProduct.id != null
        and:
            1 * productRepository.save(_) >> {
                Product product = new Product()
                product.setPrice(price)
                product.setName(name)
                return product
            }


    }

/*    findProductByName
    getAllProducts
    createProduct*/

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

    def addTwoReceiptItemsToReceiptList(Product product1, Product product2) {
        def setOfOtherProducts = []
        ReceiptItem receiptItem = new ReceiptItem()
        receiptItem.setProduct(product1)
        receiptItem.setQuantity(1)
        setOfOtherProducts.add(receiptItem)

        ReceiptItem receiptItem1 = new ReceiptItem()
        receiptItem1.setProduct(product2)
        receiptItem1.setQuantity(1)
        setOfOtherProducts.add(receiptItem1)

        return setOfOtherProducts
    }

    def creaetCombinedPromoForSampleProduct() {

        Product product1 = createSampleProduct()
        Promo combinedPromo = new Promo()
        combinedPromo.setType(PromoType.COMBINED)
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
