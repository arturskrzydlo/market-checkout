package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import com.pragmaticcoders.checkout.checkoutcomponent.general.AbstractDomainClass;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter class Receipt extends AbstractDomainClass {

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
    private Set<ReceiptItem> items = new HashSet<>();
    private boolean opened = true;
    private Double payment;

    public void addItem(ReceiptItem receiptItem) {
        receiptItem.setReceipt(this);
        items.add(receiptItem);
    }

    public void removeItem(ReceiptItem receiptItem) {
        items.remove(receiptItem);
        receiptItem.setReceipt(null);
    }
}
