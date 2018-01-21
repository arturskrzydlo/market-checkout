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


    // ========================================================================================================================================

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
            result.payment == 195

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

        ReceiptItem keyboardItem = new ReceiptItem()
        keyboardItem.setQuantity(10)
        keyboardItem.setProduct(createKeyBoardProduct())
        keyboardItem.setId(2)

        receipt.addItem(keyboardItem)

        ReceiptItem toothBrushItem = new ReceiptItem()
        toothBrushItem.setQuantity(1)
        toothBrushItem.setProduct(createToothBrushProduct())
        toothBrushItem.setId(3)

        receipt.addItem(toothBrushItem)

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

    def createCombinedPromoForReceiptItems(ReceiptItem receiptItem1, ReceiptItem receiptItem2) {

        Promo combinedPromo = new Promo()
        combinedPromo.setType(PromoType.COMBINED)
        combinedPromo.addProduct(receiptItem1.product)
        combinedPromo.addProduct(receiptItem2.product)
        combinedPromo.setSpecialPrice(receiptItem2.product.getPrice() + receiptItem1.product.getPrice() - 10.0)

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
        createCombinedPromoForReceiptItems(receipt.items[0], receipt.items[1])

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

    // ===========================================================================================================

    /*




      def createReceiptWithCombinedPromo(){

          Receipt receipt = createReceiptWithReceiptItems()
          createCombinedPromoForReceiptItems(receipt.items[0],receipt.items[1])
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

  */
}
