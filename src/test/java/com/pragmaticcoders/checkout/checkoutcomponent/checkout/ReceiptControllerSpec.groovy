package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import com.fasterxml.jackson.databind.ObjectMapper
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
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
    }

}
