package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.CashRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {

    Optional<CashRegister> findByRegisterNumber(String registerNumber);

    Boolean existsByRegisterNumber(String registerNumber);

    List<CashRegister> findByActiveTrue();

    List<CashRegister> findByIsOpenTrue();

    @Query("SELECT cr FROM CashRegister cr WHERE cr.location = :location " +
            "AND cr.deleted = false")
    List<CashRegister> findByLocation(@Param("location") String location);

    @Query("SELECT cr FROM CashRegister cr LEFT JOIN FETCH cr.sales WHERE cr.id = :id")
    Optional<CashRegister> findByIdWithSales(@Param("id") Long id);

    @Query("SELECT cr FROM CashRegister cr " +
       "LEFT JOIN FETCH cr.sales " +
       "LEFT JOIN FETCH cr.openedBy " +
       "LEFT JOIN FETCH cr.closedBy")
    List<CashRegister> findAllWithSalesAndUsers();

}