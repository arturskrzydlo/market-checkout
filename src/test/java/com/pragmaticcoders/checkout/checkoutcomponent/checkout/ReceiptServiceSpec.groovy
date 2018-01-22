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

    def headPhonesItem
    def keyboardItem
    def toothBrushItem

    @Shared
    def sampleProductToCheck = createToothBrushProduct()
    @Shared
    def promoList = createMultiPricedPromoForSingleProduct(createReceiptWithOneItem().items[0])

    Should "get product by it's name and return it's price for existing product name and existing receipt"() {
        given: "scanned product with name and quantity"
            def scannedProduct = createScannedProduct()
        and:
            def productFound = createToothBrushProduct()
            def existingReceipt = createReceiptWithOneItem()
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
            def productFound = createToothBrushProduct()
            def existingReceipt = createFreshReceipt()
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
            def productFound = createToothBrushProduct()
            def existingReceipt = createReceiptWithOneItem()
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
            def nonExistingProduct = createToothBrushProduct()
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
            def nonExistingReceipt = createFreshReceipt()
            def productFound = createToothBrushProduct()
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
            def freshReceipt = createFreshReceipt()
        when:
            def result = receiptService.createNewReceipt()
        then:
            1 * receiptRepository.save(_) >> freshReceipt
        and:
            result.getId() == freshReceipt.getId()
    }

    Should "return 0 when there are no items on receipt, when calling to calculate payment"() {
        given: "mock receipt which will be returned from repository"
            def receiptWithoutItems = createFreshReceipt()
        when: "calling method to produce receipt (calculate payment)"
            def result = receiptService.produceReceiptWithPayment(receiptWithoutItems.id)
        then:
            1 * receiptRepository.findOne(receiptWithoutItems.id) >> receiptWithoutItems
        and:
            result.getPayment() == 0.0
    }


    @Unroll
    Should "get real receipt payment after multipriced promotions application for #quantity unit of product"() {
        given: "existing receipt"
            def receipt = createFreshReceipt()
            addReceiptItemToReceipt(receipt, quantity)
            createMultiPricedPromoForSingleProduct(receipt.items[0])
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


    Should "return 0 when calling to calculate payment with null product"() {
        given: "null product"
            Product product = null
        and:
            def receipt = createReceiptWithOneItem()
            receipt.items[0].setProduct(product)
        when:
            def result = receiptService.produceReceiptWithPayment(receipt.id)
        then:
            1 * receiptRepository.findOne(receipt.id) >> receipt
            result.payment == 0.0
    }


    Should "return 0 price, when quantity is less then 1"() {
        given: "receipt wit quantity 0 for receiptitem"
            def receipt = createReceiptWithOneItem()
            receipt.items[0].setQuantity(quantity)
        when:
            def result = receiptService.produceReceiptWithPayment(receipt.id)
        then:
            1 * receiptRepository.findOne(receipt.id) >> receipt
            result.payment == finalPrice
        where:
            quantity | finalPrice
            -1       | 0
            0        | 0
    }


    Should "return combined promotion for single unit products when no other promotion exists"() {

        given: "one receipt with combined promo for a sample product"
            def receipt = createReceiptWithCombinedPromo()
        when:
            def result = receiptService.produceReceiptWithPayment(receipt.id)
        then:
            1 * receiptRepository.findOne(receipt.id) >> receipt
            result.payment == 195 //value expected

    }


    Should "return more beneficial price, when two combined promotions exists for single product"() {

        given: "one receipt with combined promo for a sample product"
            def receipt = createReceiptWith2CombinedPromos()
        when:
            def result = receiptService.produceReceiptWithPayment(receipt.id)
        then:
            1 * receiptRepository.findOne(receipt.id) >> receipt
            result.payment == 501.5 // this values comes from fact that toothbrush should be with promotion thus 1*(5.0-specialprice)=-15.0
            // headphones should be 3(3 headphones and 3 keyboardS) * specialprice(105.5) = 316.5
            // keyboard should be without promotions (because it was uses by headphone promo and tothbrush promo)
            // 10*keyboardprice = 10*20 = 200. In total 501.5
    }


    Should "return price with combined promotions result when combined and multiprice promotions exists and combined is more beneficial"() {

        given: "one receipt with combined and multipriced promo"
            def receipt = createReceiptWithMultiPriceAndCombinedPromo()
        when:
            def result = receiptService.produceReceiptWithPayment(receipt.id)
        then:
            1 * receiptRepository.findOne(receipt.id) >> receipt
            result.payment == 501.5 //because combined promotions should be cheaper (multiprice promotions is just for one toothbrush)
    }

    Should "return price with multiprice promotiont when combined and multiprice promotions exists and multiprice is more beneficial"() {

        given: "one receipt with combined and multipriced promo"
            def receipt = createReceiptWithMultiPriceAndCombinedPromoWhereMultiPriceMoreBeneficial()
        when:
            def result = receiptService.produceReceiptWithPayment(receipt.id)
        then:
            1 * receiptRepository.findOne(receipt.id) >> receipt
            result.payment == 581.5 //because multiprice promotion give 65.0 instead of 700 for combined promotion
    }

    Should "all returned items on receipt have prices assigned to them"() {

        given: "one receipt with 2 items"
            def receipt = createReceiptWithTwoItems()
        when:
            def result = receiptService.produceReceiptWithPayment(receipt.id)
        then:
            1 * receiptRepository.findOne(receipt.id) >> receipt
            checkIfAllReceiptItemHasPriceAssignedToThem(result)
    }

    Should "all returned items on receipt have prices assigned to them and it should reflect promotion applied to them"() {

        given: "one receipt with combined promo for a sample product"
            def receipt = createReceiptWithMultiPricePromoForSingleItem()
            receipt.items[0].setQuantity(30)
        when:
            def result = receiptService.produceReceiptWithPayment(receipt.id)
        then:
            1 * receiptRepository.findOne(receipt.id) >> receipt
            receipt.items[0].price == 65.0

    }

    def checkIfAllReceiptItemHasPriceAssignedToThem(receipt) {
        for (ReceiptItem receiptItem : receipt.items) {
            if (receiptItem.price == null) {
                return false
            }
        }
        return true
    }

    def createKeyBoardProduct() {

        Product keyboard = new Product()
        keyboard.setName("keyboard")
        keyboard.setPrice(20.0)

        return keyboard
    }

    def createToothBrushProduct() {

        Product toothbrush = new Product()
        toothbrush.setName("toothbrush")
        toothbrush.setPrice(5.0)

        return toothbrush
    }

    def createHeadPhonesProduct() {

        Product headphones = new Product()
        headphones.setName("headphones")
        headphones.setPrice(155.5)

        return headphones
    }

    def create2ReceiptItemsList(Receipt receipt) {

        keyboardItem = new ReceiptItem()
        keyboardItem.setQuantity(10)
        keyboardItem.setProduct(createKeyBoardProduct())
        keyboardItem.setId(2)

        receipt.addItem(keyboardItem)

        toothBrushItem = new ReceiptItem()
        toothBrushItem.setQuantity(1)
        toothBrushItem.setProduct(createToothBrushProduct())
        toothBrushItem.setId(3)

        receipt.addItem(toothBrushItem)

        return receipt
    }

    def create3ReceiptItemsList(Receipt receipt) {

        create2ReceiptItemsList(receipt)

        headPhonesItem = new ReceiptItem()
        headPhonesItem.setQuantity(3);
        headPhonesItem.setProduct(createHeadPhonesProduct())
        headPhonesItem.setId(4)

        receipt.addItem(headPhonesItem)

        return receipt
    }

    def createReceiptItem(receipt) {

        ReceiptItem keyboardItem = new ReceiptItem()
        keyboardItem.setQuantity(1)
        keyboardItem.setProduct(createToothBrushProduct())
        keyboardItem.setId(3)

        receipt.addItem(keyboardItem)
    }

    def createMultiPricedPromoForSingleProduct(ReceiptItem receiptItem1) {

        def promo1 = new Promo()
        promo1.setUnitAmount(10)
        promo1.setSpecialPrice(30)
        promo1.addProduct(receiptItem1.product)
        promo1.setType(PromoType.MULTIPRICE)

        def promo2 = new Promo()
        promo2.setUnitAmount(20)
        promo2.setSpecialPrice(35)
        promo2.addProduct(receiptItem1.product)
        promo2.setType(PromoType.MULTIPRICE)

        def promo3 = new Promo()
        promo3.setUnitAmount(5)
        promo3.setSpecialPrice(20)
        promo3.addProduct(receiptItem1.product)
        promo3.setType(PromoType.MULTIPRICE)

        List<Promo> promos = [promo1, promo2, promo3]
        return promos
    }

    def createCombinedPromoForReceiptItems(ReceiptItem receiptItem1, ReceiptItem receiptItem2, Double discount) {

        Promo combinedPromo = new Promo()
        combinedPromo.setType(PromoType.COMBINED)
        combinedPromo.addProduct(receiptItem1.product)
        combinedPromo.addProduct(receiptItem2.product)
        combinedPromo.setSpecialPrice(receiptItem2.product.getPrice() + receiptItem1.product.getPrice() - discount)

        return combinedPromo
    }

    def createFreshReceipt() {
        Receipt receipt = new Receipt()
        receipt.setId(1)
        return receipt
    }

    def createReceiptWithOneItem() {

        Receipt receipt = createFreshReceipt()
        createReceiptItem(receipt)
        return receipt
    }

    def createReceiptWithTwoItems() {
        Receipt receipt = createFreshReceipt()
        create2ReceiptItemsList(receipt)
        return receipt
    }

    def createReceiptWithMultiPricePromoForSingleItem() {

        Receipt receipt = createFreshReceipt()
        createReceiptItem(receipt)
        createMultiPricedPromoForSingleProduct(receipt.items[0])

        return receipt
    }

    def createReceiptWithCombinedPromo() {

        Receipt receipt = createFreshReceipt()
        receipt = create2ReceiptItemsList(receipt)
        createCombinedPromoForReceiptItems(receipt.items[0], receipt.items[1], 10)

        return receipt
    }

    def createReceiptWith2CombinedPromos() {

        Receipt receipt = createFreshReceipt()
        receipt = create3ReceiptItemsList(receipt)
        createCombinedPromoForReceiptItems(toothBrushItem, keyboardItem, 20)
        createCombinedPromoForReceiptItems(keyboardItem, headPhonesItem, 50)

        return receipt
    }

    def createReceiptWithMultiPriceAndCombinedPromo() {

        Receipt receipt = createFreshReceipt()
        create3ReceiptItemsList(receipt)
        createCombinedPromoForReceiptItems(toothBrushItem, keyboardItem, 20)
        createCombinedPromoForReceiptItems(keyboardItem, headPhonesItem, 50)
        createMultiPricedPromoForSingleProduct(toothBrushItem)

        return receipt
    }

    def createReceiptWithMultiPriceAndCombinedPromoWhereMultiPriceMoreBeneficial() {

        Receipt receipt = createFreshReceipt()
        create3ReceiptItemsList(receipt)
        toothBrushItem.setQuantity(30)
        toothBrushItem.getProduct().setPrice(30)
        createCombinedPromoForReceiptItems(toothBrushItem, keyboardItem, 20)
        createCombinedPromoForReceiptItems(keyboardItem, headPhonesItem, 50)
        createMultiPricedPromoForSingleProduct(toothBrushItem)

        return receipt
    }

    def createScannedProduct() {

        ScannedProductDTO scannedProductDTO = new ScannedProductDTO()
        scannedProductDTO.setProductName("toothbrush")
        scannedProductDTO.setQuantity(3)

        return scannedProductDTO
    }

    def addReceiptItemToReceipt(receipt, quantity) {
        def receiptItem = new ReceiptItem()
        receiptItem.setProduct(createToothBrushProduct())
        receiptItem.setReceipt(receipt)
        receiptItem.setQuantity(quantity)
        receipt.addItem(receiptItem)

    }
}
