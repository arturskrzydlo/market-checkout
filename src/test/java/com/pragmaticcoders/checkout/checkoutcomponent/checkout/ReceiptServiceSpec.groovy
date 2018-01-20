package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import spock.lang.Specification

import java.lang.Void as Should

class ReceiptServiceSpec extends Specification {

    def productRepository = Mock(ProductRepository)
    def receiptRepository = Mock(ReceiptRepository)
    def receiptService = new ReceiptServiceImpl(productRepository, receiptRepository)

    Should "get product by it's name and return it's price for existing product name and existing receipt"() {
        given: "scanned product with name and quantity"
            def scannedProduct = createScannedProduct()
        and:
            def productFound = createProduct()
            def existingReceipt = createReceiptWithReceiptItems()
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
            def existingReceipt = createReceiptWithReceiptItems()
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

    //todo
    //should scan quantitty 1


    def createProduct() {

        Product product = new Product()
        product.setId(1)
        product.setName("toothbrush")
        product.setPrice(5.0)

        return product

    }

    def createReceiptWithReceiptItems() {

        def receipt = createExistingReceiptWithoutReceiptItems()

        ReceiptItem receiptItem = new ReceiptItem()
        receiptItem.setQuantity(1)
        receiptItem.setProduct(createProduct())
        receiptItem.setId(1)

        receipt.addItem(receiptItem)
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
