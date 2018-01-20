package com.pragmaticcoders.checkout.checkoutcomponent.checkout;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter class Receipt extends AbstractDomainClass {

    @OneToMany(mappedBy = "receipt")
    private Set<ReceiptItem> items = new HashSet<>();

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
