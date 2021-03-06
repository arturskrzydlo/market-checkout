package com.pragmaticcoders.checkout.checkoutcomponent.products;

import com.pragmaticcoders.checkout.checkoutcomponent.general.AbstractDomainClass;
import com.pragmaticcoders.checkout.checkoutcomponent.promo.Promo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(exclude = "promos")
public class Product extends AbstractDomainClass {

    @Column(unique = true)
    private String name;
    private Double price;
    @ManyToMany(mappedBy = "products")
    private Set<Promo> promos = new HashSet<>();

    public void addPromo(Promo promo) {
        promos.add(promo);
    }

    public void removePromo(Promo promo) {
        promos.remove(promo);
    }

}
