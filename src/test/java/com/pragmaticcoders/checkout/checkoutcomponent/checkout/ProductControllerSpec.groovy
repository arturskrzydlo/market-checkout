package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.lang.Void as Should

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(ProductController)
class ProductControllerSpec extends Specification {

    @Autowired
    private MockMvc mockMvc

    @MockBean
    private ProductService productService

    @MockBean
    private PromoService promoService

/*    Should "return json with all existing products"() {

        given: "preloaded products to application database"
            def products = createProductList()
        when:
            def result = this.mockMvc.perform(get("/checkout/products"))
        then:
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.*').value(Matchers.hasSize(products.size())))
        and:
            1 * productService.getAllProducts(_) >> products

    }*/

    Should "return json with no products when products were not preloaded to application"() {

        when:
            def result = this.mockMvc.perform(get("/products"))
        then:
            result.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath('$.*').value(Matchers.hasSize(0)))


    }

    def createProductList() {

        Product product1 = new Product();
        product1.setId(1)
        product1.setName("toothbrush")
        product1.setPrice(5.0)

        Product product2 = new Product()
        product2.setId(2)
        product2.setName("vacuumcleaner")
        product2.setPrice(200.0)

        [product1, product2]

    }
}
