package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import com.fasterxml.jackson.databind.ObjectMapper
import com.pragmaticcoders.checkout.checkoutcomponent.products.ProductService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification

import javax.transaction.Transactional
import java.lang.Void as Should

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ScanningProductsAcceptSpec extends Specification {

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private ReceiptService receiptService

    @Autowired
    private ObjectMapper mapper

    @Autowired
    private ProductService productService


    Should "user can sucessfully open new receipt"() {

        when: "new receipt is beign created"
            def result = mockMvc.perform(MockMvcRequestBuilders.post("/receipt"))
        then: "empty receipt with id is returned"
            result.andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.receiptId').isNotEmpty())
                    .andExpect(jsonPath('$.items').isEmpty())
                    .andExpect(jsonPath('$.payment').doesNotExist())
    }

    @Transactional
    Should "user can sucessfully scan product, which return actual product price and receipt is updated with this product"() {
        given: "product to scan"
            def productForScanning = new ScannedProductDTO()
            productForScanning.productName = "toothbrush"
            productForScanning.quantity = 2
        and: "existing empty receipt"
            def receipt = createEmptyReceipt()
            receiptService.produceReceiptWithPayment(receipt.getId()).getItems()
        and: "existing product in stock"
            def productInStock = createNewProduct(productForScanning.productName, 5.0)
        when:
            def result = mockMvc.perform(MockMvcRequestBuilders.patch("/receipt/" + receipt.id)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(mapper.writeValueAsString(productForScanning)))
        then: "produtct with its normal price is returned"
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.price').value(productInStock.get().price * productForScanning.quantity))
        and: "receipt list has been updated and it's size is now equals to one"
            receiptService.produceReceiptWithPayment(receipt.getId()).getItems().size() == 1

    }

    Should "user have no possibility to add product without creating receipt firstly"() {
        given:
        given: "product to scan"
            def productForScanning = new ScannedProductDTO()
            productForScanning.productName = "toothbrush"
            productForScanning.quantity = 2
        when:
            def result = mockMvc.perform(MockMvcRequestBuilders.patch("/receipt/")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(mapper.writeValueAsString(productForScanning)))
        then:
            result.andExpect(status().isMethodNotAllowed())
    }

    Should "user have no possibility to add product to not existing receipt"() {
        given: "product to scan"
            def productForScanning = new ScannedProductDTO()
            productForScanning.productName = "toothbrush"
            productForScanning.quantity = 2
        and: "product exists in stock"
            createNewProduct(productForScanning.productName, 5.0)
        and: "not existing receipt id"
            def receiptId = 1
        when:
            def result = mockMvc.perform(MockMvcRequestBuilders.patch("/receipt/" + receiptId)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(mapper.writeValueAsString(productForScanning)))
        then:
            result.andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.status').value(equalTo(HttpStatus.NOT_FOUND.name())))
                    .andExpect(jsonPath('$.timestamp').isNotEmpty())
                    .andExpect(jsonPath('$.message').isNotEmpty())
                    .andExpect(jsonPath('$.subErrors').value(hasSize(1)))
                    .andExpect(jsonPath('$.subErrors[0].rejectedValue').value(equalTo(receiptId.toString())))
    }

    Should "user have no possibility to add product to receipt when product doesn't exists in stock"() {
        given: "not existing product"
            def productForScanning = new ScannedProductDTO()
            productForScanning.productName = "toothbrush"
            productForScanning.quantity = 2
        and:
            def receiptId = 1
        when:
            def result = mockMvc.perform(MockMvcRequestBuilders.patch("/receipt/" + receiptId)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(mapper.writeValueAsString(productForScanning)))
        then:
            result.andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.status').value(equalTo(HttpStatus.NOT_FOUND.name())))
                    .andExpect(jsonPath('$.timestamp').isNotEmpty())
                    .andExpect(jsonPath('$.message').isNotEmpty())
                    .andExpect(jsonPath('$.subErrors').value(hasSize(1)))
                    .andExpect(jsonPath('$.subErrors[0].rejectedValue').value(equalTo(productForScanning.productName)))
    }

/*    Should "can back to receipt, change it and do calculation once again until receipt exists in a system"(){

    }*/

    def createNewProduct(String name, Double price) {
        productService.createProduct(name, price)
    }

    def createEmptyReceipt() {
        receiptService.createNewReceipt()
    }

}
