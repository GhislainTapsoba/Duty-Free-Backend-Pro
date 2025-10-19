package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_phone", columnList = "phone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(unique = true, length = 100)
    private String email;

    @Column(unique = true, length = 20)
    private String phone;

    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(length = 50)
    private String nationality;

    @Column(length = 100)
    private String passportNumber;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 50)
    private String country;

    @Column(length = 20)
    private String postalCode;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    private LoyaltyCard loyaltyCard;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Sale> sales = new ArrayList<>();

    @Column(length = 100)
    private String badgeNumber;

    @Column(length = 100)
    private String companyName;

    @Column(name = "is_vip", nullable = false)
    private Boolean isVIP = false;
}