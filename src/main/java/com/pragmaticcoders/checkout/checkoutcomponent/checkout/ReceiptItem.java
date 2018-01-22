package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter class ReceiptItem extends AbstractDomainClass {

    //TODO: add nullable false - overall check for validation and nullable fields
    @ManyToOne
    private Product product;
    @ManyToOne
    private Receipt receipt;
    private int quantity;
    private Double price;

}
