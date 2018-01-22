package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.pragmaticcoders.checkout.checkoutcomponent.general.AbstractDomainClass;
import com.pragmaticcoders.checkout.checkoutcomponent.products.Product;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter class ReceiptItem extends AbstractDomainClass {

    @ManyToOne
    private Product product;
    @ManyToOne(cascade = CascadeType.ALL)
    private Receipt receipt;
    private int quantity;
    private Double price;

}
