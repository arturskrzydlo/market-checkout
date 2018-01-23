# market-checkout

## 1. How to run application  ##

run mvnw.cmd from market-checkout directory (for Windows) or mvn (for Linux)

mvnw.cmd spring-boot:run

## 2. How to configure application ##

Application can work with profile application or without this profile. With 'application' profile some sample bootstrap data will be loaded
to application. Actually three products along with promos. When you want to run acceptance test, please, remove 

`     profiles:
      active: application
 `
 
 these lines from application.yml file.
 
 ## 3. Technologies used ##
 
 - Java 8
 - SpringBoot
 - Spring Data
 - Lombok
 - ModelMapper
 - Spock
 
 Rationale for all of them is simple : quick development 
 
 ## 4. Assumptions - what checkout component do ##
 
 Checkout component is REST component which handle simple market checkout operations. By market I mean real market like Tesco for
 instance. I was trying to imagine simple scenario, when new customer is processed. Such scenario consists of few steps. 
 
 - new receipt is created/opened
 - products are scanned one by one or product is scanned and number of the same amount of products is specified
   By this operation product is added to receipt
 - receipt is calculated - then all promotions available are applied to products which are on receipt and customer receive receipt
 - receipt is closed
 
 Additionally customer could ask only about standard product price by specyfing product name without opening new receipt
 
 Regard this assumption two entry point has been created
 
 - /checkout/receipts
 - /checkout/products
 
 With this resources and http verbs is possible to complete scenario described above
 
 For sake of simplicity I assume that security is not main concern of this application, thus everyone can modify created receipts
 For other reason (time :):) I also limit some other features which would be required for professional component. First of all it suffer for lack
 of documentation like HATEOAS or Swagger. There should be also maybe some more integration tests like for serialization/deserialization of JSON objects
 or @DataJpaTest
 
 Other important thing is algorithm for choosing promotion. I've assumed that we have two kind of promotions : Combined (Product cost cheaper
 when bought with some other product) and MultiPriced (Product is cheaper when bought with greater amount of units). Both can't connect to each other.
 Additionally Combined promotions can't connect to each other as well. Only most beneficial Combined promo for product can be choosen. For Multipriced algorithm works
 quite well, it should count best possible option. For Combined I had a lot of issues with different corner cases, thus I've decided to implement 
 simplified version which just take biggest special price (special price is price for combined product).
 
 ## 5. Using application ##
 
 The best usage of application is with postman or curl, to call http request to checkout component. We could do this in following steps (unfortunately missing HATEOAS :( )
 
 - GET /checkout/products  - get all products, with their names, prices and assigned promos
 - POST /checkout/receipts - create new receipt, to add products to that receipt
 - PATCH /checkout/receipts/1/products - update products list (empty at the beginning) with new scanned product , for instance  {"quantity":3,"product_name":"keyboard"}
 - GET /checkout/receipts/1 - get receipt with calculated final payment
 - PATCH /checkout/receipt/1 - { "opened":"false" } -close receipt
 
 ## 6. Code structure ##
 
 Firstly I wanted to make only one/two points of entry to module. This would be Controllers for receipts and products resources. Unfortunately
 when number of classes grown some structure was needed. Because of that some classes became public.
 Beside that, most important are two controllers:
 
 - ReceiptController
 - ProductController
 
 and their buisness services. Project has simple structure which consists from 3 business modules (checkout,product,promo) and one for general usage,
 which has some common abstractions, error handling etc. Acceptance tests are placed in ScanningProductsAcceptSpec
 
 I was trying to write this component with TDD approach, however it was not fully tdd, there were some parts, especially at the end of development, where I firstly write implementations, and tests later
 
