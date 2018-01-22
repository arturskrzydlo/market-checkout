package com.pragmaticcoders.checkout.checkoutcomponent.checkout

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification

import java.lang.Void as Should

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ScanningProductsAcceptSpec extends Specification {

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private ReceiptService productService


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

/*    Should "can back to receipt, change it and do calculation once again until receipt exists in a system"(){

    }*/
}
