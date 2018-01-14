package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter class Product extends AbstractDomainClass {

    private String name;
    private Double price;
    private int amountInStorage;
}
