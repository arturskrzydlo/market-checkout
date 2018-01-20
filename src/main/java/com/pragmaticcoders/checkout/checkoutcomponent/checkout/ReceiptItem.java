package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
public class ReceiptItem extends AbstractDomainClass {

    @ManyToOne
    private Product product;
    @ManyToOne
    private Receipt receipt;
    private int quantity;

}
