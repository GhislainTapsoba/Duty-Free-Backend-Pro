package com.djbc.dutyfree.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;  // ← AJOUTEZ
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(unique = true, length = 50)
    private String code;

    @Column(length = 100)
    private String contactPerson;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 50)
    private String country;

    @Column(length = 50)
    private String postalCode;

    @Column(length = 50)
    private String taxId;

    @Column(length = 200)
    private String paymentTerms;

    private java.math.BigDecimal creditLimit;

    @Column(length = 1000)
    private String notes;

    private Boolean active = true;

    @JsonIgnore  // ← AJOUTEZ CETTE LIGNE
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<PurchaseOrder> purchaseOrders = new ArrayList<>();
}