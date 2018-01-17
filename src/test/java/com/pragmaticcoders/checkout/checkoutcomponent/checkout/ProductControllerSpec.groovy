package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import java.lang.Void as Should

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(ProductController)
class ProductControllerSpec extends Specification {

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private ProductService productService

    @Autowired
    private PromoService promoService

    Should "return json with all existing products"() {

        given: "preloaded products to application database"
            def products = createProductList()
        when:
            def result = this.mockMvc.perform(get("/products"))
        then:
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.*').value(Matchers.hasSize(products.size())))
        and:
            1 * productService.getAllProducts() >> products
    }

    Should "return json with no products when products were not preloaded to application"() {

        given:
            productService.getAllProducts() >> new ArrayList<Product>()
        when:
            def result = this.mockMvc.perform(get("/products"))
        then:
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.*').value(Matchers.hasSize(0)))
    }

    Should "return actual price for a given, existing product name "() {
        given:
            def product = createProductList()[0]
        when:
            def result = this.mockMvc.perform(get("/products/" + product.getName()))
        then:
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.price').value(product.getPrice()))
                    .andExpect(jsonPath('$.*').value(hasSize(1)))
        and:
            1 * productService.findActualPriceForProduct(product.getName()) >> product.getPrice()
    }

    Should "return HttpStatus.NOT_FOUND for not existing product name"() {

        given:
            def productName = "notExistingProduct"
        when:
            def result = this.mockMvc.perform(get("/products/" + productName))
        then:
            result.andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.status').value(equalTo(HttpStatus.NOT_FOUND.name())))
                    .andExpect(jsonPath('$.timestamp').isNotEmpty())
                    .andExpect(jsonPath('$.message').isNotEmpty())
                    .andExpect(jsonPath('$.subErrors').value(hasSize(1)))
                    .andExpect(jsonPath('$.subErrors[0].rejectedValue').value(equalTo(productName)))
        and:
            1 * productService.findActualPriceForProduct(productName) >> {
                throw new ProductNotFoundException(productName)
            }
    }

    Should "return price for given existing product name which has no promos assigned to"() {

        given: "product with no promotions"
            def product = createProductList()[0]
        and: "product quantity"
            def quantity = 10
        and: "expected product price"
            def expectedProductPrice = product.getPrice() * quantity
        when:
            def result = mockMvc.perform(MockMvcRequestBuilders.get("/products/" + product.getName() + "?quantity=" + quantity))

        then:
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.price').value(expectedProductPrice))
                    .andExpect(jsonPath('$.*').value(hasSize(1)))
        and:
            1 * productService.countProductPriceWithPromotions(product.getName(), quantity) >> expectedProductPrice

    }


    def createProductList() {

        Product product1 = new Product()
        product1.setId(1)
        product1.setName("toothbrush")
        product1.setPrice(5.0)

        Product product2 = new Product()
        product2.setId(2)
        product2.setName("vacuumcleaner")
        product2.setPrice(200.0)

        [product1, product2]

    }

    @TestConfiguration
    static class MockConfig {
        def detachedMockFactory = new DetachedMockFactory()

        @Bean
        ProductService productService() {
            return detachedMockFactory.Mock(ProductService)
        }

        @Bean
        PromoService promoService() {
            return detachedMockFactory.Mock(PromoService)
        }
    }
}
