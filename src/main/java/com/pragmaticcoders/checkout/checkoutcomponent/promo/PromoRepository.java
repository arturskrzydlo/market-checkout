package com.pragmaticcoders.checkout.checkoutcomponent.promo;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface PromoRepository extends CrudRepository<Promo, Integer> {

    List<Promo> findByProducts_Name(String productName);
}
