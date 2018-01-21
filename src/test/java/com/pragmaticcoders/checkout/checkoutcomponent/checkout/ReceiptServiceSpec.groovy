package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.Void as Should

class ReceiptServiceSpec extends Specification {

    def productRepository = Mock(ProductRepository)
    def receiptRepository = Mock(ReceiptRepository)
    def productService = Mock(ProductService)
    def receiptService = new ReceiptServiceImpl(productRepository, receiptRepository, productService)

    @Shared
    def sampleProductToCheck = createProduct()
    @Shared
    def promoList = createManyMultiPricedPromosForSampleProduct(sampleProductToCheck)

    Should "get product by it's name and return it's price for existing product name and existing receipt"() {
        given: "scanned product with name and quantity"
            def scannedProduct = createScannedProduct()
        and:
            def productFound = createProduct()
            def existingReceipt = createReceiptWithReceiptItem()
        and: "repository return receipt with cart item for product"
            receiptRepository.findOne(existingReceipt.getId()) >> existingReceipt
        when: "add product has been called"
            def result = receiptService.addProductToReceipt(scannedProduct, existingReceipt.getId())
        then:
            1 * productRepository.findByName(scannedProduct.getProductName()) >> productFound
        and:
            result == productFound.getPrice() * scannedProduct.quantity

    }

    Should "add new receiptitem with product when it didn't exists previously on the receipt"() {
        given: "scanned product with name and quantity"
            def scannedProduct = createScannedProduct()
        and:
            def productFound = createProduct()
            def existingReceipt = createExistingReceiptWithoutReceiptItems()
        and: "repository return receipt with cart item for product"
            receiptRepository.findOne(existingReceipt.getId()) >> existingReceipt
        when: "add product has been called"
            receiptService.addProductToReceipt(scannedProduct, existingReceipt.getId())
        then:
            1 * productRepository.findByName(scannedProduct.getProductName()) >> productFound
        and:
            1 * receiptRepository.save(_) >> {
                Receipt receipt1 ->
                    assert receipt1.getItems().size() == 1
                    assert receipt1.getItems()[0].getId() == null

            }
    }

    Should "add update receiptitem with product when it existted previously on the receipt"() {
        given: "scanned product with name and quantity"
            def scannedProduct = createScannedProduct()
        and:
            def productFound = createProduct()
            def existingReceipt = createReceiptWithReceiptItem()
        and: "repository return receipt with cart item for product"
            receiptRepository.findOne(existingReceipt.getId()) >> existingReceipt
        when: "add product has been called"
            receiptService.addProductToReceipt(scannedProduct, existingReceipt.getId())
        then:
            1 * productRepository.findByName(scannedProduct.getProductName()) >> productFound
        and:
            1 * receiptRepository.save(_) >> {
                Receipt receipt1 ->
                    assert receipt1.getItems().size() == 1
                    assert receipt1.getItems()[0].getId() != null
            }
    }

    Should "throw ProductNotFoundException if product name doesn't exist"() {
        given: "scanned product with name and quantity"
            def scannedProduct = createScannedProduct()
        and:
            def nonExistingProduct = createProduct()
        and:
            productRepository.findByName(scannedProduct.getProductName()) >> null
        when:
            receiptService.addProductToReceipt(scannedProduct, 1)
        then:
            ProductNotFoundException exception = thrown()
            exception.message == "Product with identity " + nonExistingProduct.getName() + " does not exists"
    }

    Should "throw ReceiptNotFoundException if product name doesn't exists"() {
        given: "scanned product with name and quantity"
            def scannedProduct = createScannedProduct()
        and:
            def nonExistingReceipt = createExistingReceiptWithoutReceiptItems()
            def productFound = createProduct()
        and:
            productRepository.findByName(scannedProduct.getProductName()) >> productFound
        and:
            receiptRepository.findOne(nonExistingReceipt.getId()) >> null
        when:
            receiptService.addProductToReceipt(scannedProduct, nonExistingReceipt.getId())
        then:
            ReceiptNotFoundException exception = thrown()
            exception.message == "Receipt with identity " + nonExistingReceipt.getId() + " does not exists"
    }

    Should "create new entity with id assigned to it"() {
        given: "expected result"
            def freshReceipt = createExistingReceiptWithoutReceiptItems()
        when:
            def result = receiptService.createNewReceipt()
        then:
            1 * receiptRepository.save(_) >> freshReceipt
        and:
            result.getId() == freshReceipt.getId()
    }

    Should "return 0 when there are no items on receipt, when calling to calculate payment"() {
        given: "mock receipt which will be returned from repository"
            def receiptWithoutItems = createExistingReceiptWithoutReceiptItems()
        when: "calling method to produce receipt (calculate payment)"
            def result = receiptService.produceReceiptWithPayment(receiptWithoutItems.id)
        then:
            1 * receiptRepository.findOne(receiptWithoutItems.id) >> receiptWithoutItems
        and:
            result.getPayment() == 0.0
    }

    Should "return receipt with total price for receipt's items when multipriced promo "() {
        given: "mock receipt which will be returned from repository"
            def receiptWithItemsWithPromos = createReceiptWithReceiptItemsWithPromos()
        when: "calling method to produce receipt (calculate payment)"
            def result = receiptService.produceReceiptWithPayment(receiptWithItemsWithPromos.id)
        then:
            1 * receiptRepository.findOne(receiptWithItemsWithPromos.id) >> receiptWithItemsWithPromos
        and:
            result.getPayment() == 25 // first item special price is 20 second is 5

    }

    // ========================================================================================================================================

    @Unroll
    Should "get real product price after promotions application for #quantity unit of product"() {
        given: "existing receipt"
            def receipt = createFreshReceipt()
            addReceiptItemToReceipt(receipt, quantity)
        when:
            def result = receiptService.produceReceiptWithPayment(receipt.id)
        then:
            1 * receiptRepository.findOne(receipt.id) >> receipt
            result.payment == finalPrice
        where:
            quantity | finalPrice
            1        | sampleProductToCheck.getPrice()
            5        | promoList[2].getSpecialPrice()
            7        | promoList[2].getSpecialPrice() + 2 * sampleProductToCheck.getPrice()
            10       | promoList[0].getSpecialPrice()
            11       | promoList[0].getSpecialPrice() + sampleProductToCheck.getPrice()
            15       | promoList[0].getSpecialPrice() + promoList[2].getSpecialPrice()
            20       | promoList[1].getSpecialPrice()
            30       | promoList[1].getSpecialPrice() + promoList[0].getSpecialPrice()
            100      | 5 * promoList[1].getSpecialPrice()
            115      | 5 * promoList[1].getSpecialPrice() + promoList[0].getSpecialPrice() + promoList[2].getSpecialPrice()

    }

/*
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


    Should "return combined promotion for single unit products when no other promotion exists"() {

        given: "one combined promo for a sample product"
            def combinedPromo = creaetCombinedPromoForSampleProduct()
        and: "add two receipt items to a receipt"
            def setOfOtherProducts = addTwoReceiptItemsToReceiptList(combinedPromo.getProducts()[0], combinedPromo.getProducts()[1])
        when:
            def result = producteService.countProductPriceWithPromotions(combinedPromo.getProducts()[0], 1, setOfOtherProducts as Set<ReceiptItem>)
        then:
            result == combinedPromo.getSpecialPrice()


    }
*/


    def addReceiptItemToReceipt(receipt, quantity) {
        def receiptItem = new ReceiptItem()
        receiptItem.setProduct(sampleProductToCheck)
        receiptItem.setReceipt(receipt)
        receiptItem.setQuantity(quantity)
        receipt.addItem(receiptItem)
    }

    def createFreshReceipt() {

        Receipt receipt = new Receipt()
        receipt.setId(1)
        return receipt
    }

    def createManyMultiPricedPromosForSampleProduct(product) {

        def promo1 = new Promo()
        promo1.setUnitAmount(10)
        promo1.setSpecialPrice(30)
        promo1.addProduct(product)
        promo1.setType(PromoType.MULTIPRICE)

        def promo2 = new Promo()
        promo2.setUnitAmount(20)
        promo2.setSpecialPrice(35)
        promo2.addProduct(product)
        promo2.setType(PromoType.MULTIPRICE)

        def promo3 = new Promo()
        promo3.setUnitAmount(5)
        promo3.setSpecialPrice(20)
        promo3.addProduct(product)
        promo3.setType(PromoType.MULTIPRICE)

        List<Promo> promos = [promo1, promo2, promo3]
        return promos

    }

    def createReceiptWithReceiptItemsWithPromos() {

        Receipt receipt = createReceiptWithReceiptItems()

        Promo promo1 = new Promo()
        promo1.setUnitAmount(10)
        promo1.setSpecialPrice(20.0)
        promo1.setType(PromoType.MULTIPRICE)
        promo1.addProduct(receipt.getItems()[0].product)

        Promo promo2 = new Promo()
        promo2.setUnitAmount(20)
        promo2.setSpecialPrice(35)
        promo2.setType(PromoType.MULTIPRICE)
        promo2.addProduct(receipt.getItems()[1].product)

        return receipt
    }

    def createProduct() {

        Product product = new Product()
        product.setId(1)
        product.setName("toothbrush")
        product.setPrice(5.0)

        return product
    }

    def calculateExpectedSimpleReceiptPayment(Receipt receipt) {
        def sum = 0
        for (ReceiptItem item : receipt.items) {
            sum = sum + item.quantity * item.product.price
        }
        receipt.setPayment(sum)
        return receipt
    }

    def createReceiptWithReceiptItem() {

        def receipt = createExistingReceiptWithoutReceiptItems()

        ReceiptItem receiptItem = new ReceiptItem()
        receiptItem.setQuantity(1)
        receiptItem.setProduct(createProduct())
        receiptItem.setId(1)

        receipt.addItem(receiptItem)
        return receipt
    }

    def createReceiptWithReceiptItems() {

        def receipt = createReceiptWithReceiptItem()

        Product keyboard = new Product()
        keyboard.setName("keyboard")
        keyboard.setPrice(20.0)

        ReceiptItem keyboardItem = new ReceiptItem()
        keyboardItem.setQuantity(10)
        keyboardItem.setProduct(keyboard)
        keyboardItem.setId(2)

        receipt.addItem(keyboardItem)

        return receipt
    }


    def createExistingReceiptWithoutReceiptItems() {

        Receipt receipt = new Receipt()
        receipt.setId(1)
        return receipt
    }

    def createScannedProduct() {

        ScannedProductDTO scannedProductDTO = new ScannedProductDTO()
        scannedProductDTO.setProductName("toothbrush")
        scannedProductDTO.setQuantity(3)

        return scannedProductDTO
    }
}
