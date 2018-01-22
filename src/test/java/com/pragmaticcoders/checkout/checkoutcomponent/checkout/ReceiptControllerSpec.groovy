package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import com.fasterxml.jackson.databind.ObjectMapper
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import java.lang.Void as Should

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(ReceiptController)
class ReceiptControllerSpec extends Specification {

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private ObjectMapper mapper

    @Autowired
    private ReceiptService receiptService


    Should "should add product to a receipt and return it's price for existing product and existing receipt"() {

        given: "Product which exists in application"
            def product = createProduct()
            def scannedProduct = createScannedProduct()
            def expectedResult = product.getPrice() * scannedProduct.getQuantity()
            def existingReceiptId = 1
        when: "product is scanned"
            def result = mockMvc.perform(put("/receipt/" + existingReceiptId)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(mapper.writeValueAsString(scannedProduct)))
        then:
            1 * receiptService.addProductToReceipt(scannedProduct, existingReceiptId) >> expectedResult
        and:
            result.andExpect(status().isOk())
            result.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            result.andExpect(jsonPath('$.price').value(expectedResult))
            result.andExpect(jsonPath('$.*').value(hasSize(1)))

    }

    Should "return HttpStatus.NOT_FOUND for not existing product name when scanning product"() {

        given:
            def scannedProduct = createScannedProduct()
            def receiptId = 10
        when:
            def result = this.mockMvc.perform(put("/receipt/" + receiptId)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(mapper.writeValueAsString(scannedProduct)))
        then:
            result.andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.status').value(equalTo(HttpStatus.NOT_FOUND.name())))
                    .andExpect(jsonPath('$.timestamp').isNotEmpty())
                    .andExpect(jsonPath('$.message').isNotEmpty())
                    .andExpect(jsonPath('$.subErrors').value(hasSize(1)))
                    .andExpect(jsonPath('$.subErrors[0].rejectedValue').value(equalTo(scannedProduct.getProductName())))
        and:
            1 * receiptService.addProductToReceipt(scannedProduct, receiptId) >> {
                throw new ProductNotFoundException(scannedProduct.getProductName())
            }
    }

    Should "return HttpStatus.NOT_FOUND for not existing receipt name when scanning product"() {
        given:
            def scannedProduct = createScannedProduct()
            def receiptId = 10
        when:
            def result = this.mockMvc.perform(put("/receipt/" + receiptId)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(mapper.writeValueAsString(scannedProduct)))
        then:
            result.andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.status').value(equalTo(HttpStatus.NOT_FOUND.name())))
                    .andExpect(jsonPath('$.timestamp').isNotEmpty())
                    .andExpect(jsonPath('$.message').isNotEmpty())
                    .andExpect(jsonPath('$.subErrors').value(hasSize(1)))
                    .andExpect(jsonPath('$.subErrors[0].rejectedValue').value(equalTo(receiptId.toString())))
        and:
            1 * receiptService.addProductToReceipt(scannedProduct, receiptId) >> {
                throw new ReceiptNotFoundException(receiptId)
            }
    }

    Should "create new receipt with id assigned to it when request to create receipt comes to the api"() {
        given: "expected result in form of new receipt"
            def freshlyCreatedReceipt = createFreshReceipt()
        when: "proper call for creating new receipt resource incomes"
            def result = mockMvc.perform(post("/receipt"))
        then: "new receipt with assigned id in response"
            result.andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.receiptId').value(equalTo(freshlyCreatedReceipt.getId().toString())))

        and: "service create new receipt with id"
            1 * receiptService.createNewReceipt() >> freshlyCreatedReceipt
    }

    Should "return receipt with total price for shopping and detailes list of product and their prices, when calling for receipt billing"() {
        given: "existing receipt with receipt items"
            def receipt = createReceiptWithReceiptItems()
            def expectedReceiptWithPayment = calculateExpectedSimpleReceiptPayment(receipt)
        when:
            def result = mockMvc.perform(get("/receipt/" + receipt.getId()))
        then:
            1 * receiptService.produceReceiptWithPayment(receipt.getId()) >> expectedReceiptWithPayment
        and:
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.payment').value(equalTo(expectedReceiptWithPayment.payment)))
                    .andExpect(jsonPath('$.items').value(hasSize(expectedReceiptWithPayment.items.size())))
                    .andExpect(jsonPath('$.items[?(@.product.name==\'' + receipt.items[0].product.name + '\')]').exists())
                    .andExpect(jsonPath('$.items[?(@.price==' + receipt.items[0].product.price * receipt.items[0].quantity + ')]').exists())
                    .andExpect(jsonPath('$.items[?(@.price==' + receipt.items[1].product.price * receipt.items[1].quantity + ')]').exists())
                    .andExpect(jsonPath('$.items[?(@.product.name==\'' + receipt.items[1].product.name + '\')]').exists())

    }

    Should "return receipt with total price for shopping equals 0 and no list of product, when calling for receipt billing without any items on receipt (freshly created receipt)"() {
        given: "existing receipt"
            def receipt = createFreshReceipt()
            def expectedReceiptWithPayment = calculateExpectedSimpleReceiptPayment(receipt)
        when:
            def result = mockMvc.perform(get("/receipt/" + receipt.getId()))
        then:
            1 * receiptService.produceReceiptWithPayment(receipt.getId()) >> expectedReceiptWithPayment
        and:
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.payment').value(equalTo(expectedReceiptWithPayment.payment)))
                    .andExpect(jsonPath('$.items').isEmpty())

    }

    Should "return HttpStatus.NOT_FOUND for not existing receipt name when calling for receipt billing"() {
        given: "not existing receipt"
            def notExistingReceipt = createFreshReceipt()
        when:
            def result = mockMvc.perform(get("/receipt/" + notExistingReceipt.getId()))
        then:
            1 * receiptService.produceReceiptWithPayment(notExistingReceipt.getId()) >> {
                throw new ReceiptNotFoundException(notExistingReceipt.id)
            }
        and:
            result.andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.status').value(equalTo(HttpStatus.NOT_FOUND.name())))
                    .andExpect(jsonPath('$.timestamp').isNotEmpty())
                    .andExpect(jsonPath('$.message').isNotEmpty())
                    .andExpect(jsonPath('$.subErrors').value(hasSize(1)))
                    .andExpect(jsonPath('$.subErrors[0].rejectedValue').value(equalTo(notExistingReceipt.id.toString())))
    }


    def createProduct() {

        Product product = new Product()
        product.setId(1)
        product.setName("toothbrush")
        product.setPrice(5.0)

        return product
    }

    def createScannedProduct() {

        ScannedProductDTO scannedProductDTO = new ScannedProductDTO()
        scannedProductDTO.setProductName("toothbrush")
        scannedProductDTO.setQuantity(3)

        return scannedProductDTO
    }

    def createFreshReceipt() {

        Receipt receipt = new Receipt()
        receipt.setId(1)
        return receipt
    }

    def createReceiptWithReceiptItems() {

        Receipt receipt = new Receipt()
        receipt.setId(1)

        ReceiptItem toothBrushItem = new ReceiptItem()
        toothBrushItem.setQuantity(3)
        toothBrushItem.setProduct(createProduct())
        toothBrushItem.setId(1)
        toothBrushItem.setPrice(toothBrushItem.getQuantity() * toothBrushItem.product.price)

        ReceiptItem vacuumCleanerItem = new ReceiptItem()
        vacuumCleanerItem.setQuantity(1)

        Product vacuumCleaner = createProduct()
        vacuumCleaner.setPrice(200.0)
        vacuumCleaner.setName("vacuumcleaner")

        vacuumCleanerItem.setProduct(vacuumCleaner)
        vacuumCleanerItem.setPrice(vacuumCleanerItem.quantity * vacuumCleanerItem.product.price)

        receipt.addItem(toothBrushItem)
        receipt.addItem(vacuumCleanerItem)

        return receipt
    }

    def calculateExpectedSimpleReceiptPayment(Receipt receipt) {
        def sum = 0
        for (ReceiptItem item : receipt.items) {
            sum = sum + item.quantity * item.product.price
        }
        receipt.setPayment(sum)
        return receipt
    }

    @TestConfiguration
    static class MockConfig {
        def detachedMockFactory = new DetachedMockFactory()

        @Bean
        ObjectMapper getJacksonObjectMapper() {
            MappingJackson2HttpMessageConverter jacksonMessageConverter = new MappingJackson2HttpMessageConverter();
            return jacksonMessageConverter.getObjectMapper()
        }

        @Bean
        ReceiptService cartService() {
            return detachedMockFactory.Mock(ReceiptService)
        }

        @Bean
        ModelMapper getModelMapper() {
            return new ModelMapper()
        }
    }

}
