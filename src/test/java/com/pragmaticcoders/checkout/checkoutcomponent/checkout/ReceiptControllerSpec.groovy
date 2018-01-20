package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import java.lang.Void as Should

import static org.hamcrest.Matchers.hasSize
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
            def result = mockMvc.perform(MockMvcRequestBuilders.put("/receipt/" + existingReceiptId)
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