package com.pragmaticcoders.checkout.checkoutcomponent.promo;

import com.pragmaticcoders.checkout.checkoutcomponent.general.AbstractDomainClass;
import com.pragmaticcoders.checkout.checkoutcomponent.products.Product;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(exclude = "products")
public class Promo extends AbstractDomainClass {

    private Double specialPrice;
    private int unitAmount;
    private PromoType type;

    @Transient
    private boolean used = false;

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
