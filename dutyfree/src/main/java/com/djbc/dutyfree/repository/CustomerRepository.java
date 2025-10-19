package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByPassportNumber(String passportNumber);

    Boolean existsByEmail(String email);

    Boolean existsByPhone(String phone);

    List<Customer> findByActiveTrue();

    List<Customer> findByIsVIPTrue();

    @Query("SELECT c FROM Customer c WHERE c.deleted = false AND " +
            "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Customer> searchCustomers(@Param("search") String search);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.loyaltyCard WHERE c.id = :id")
    Optional<Customer> findByIdWithLoyaltyCard(@Param("id") Long id);
}