package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(exclude = "products") class Promo extends AbstractDomainClass {

    private Double specialPrice;
    private int unitAmount;

    @ManyToMany
    @JoinTable(
            name = "Promo_product",
            joinColumns = {@JoinColumn(name = "promo_id")},
            inverseJoinColumns = {@JoinColumn(name = "product_id")}
    )
    private Set<Product> products = new HashSet<>();

    public void addProduct(Product product) {
        products.add(product);
        product.addPromo(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.removePromo(this);
    }

}
