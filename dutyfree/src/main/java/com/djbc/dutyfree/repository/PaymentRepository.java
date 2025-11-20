package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.Payment;
import com.djbc.dutyfree.domain.enums.Currency;
import com.djbc.dutyfree.domain.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ✅ CORRIGÉ : Utilisation de JPQL car Payment a un champ "sale" pas "saleId"
    @Query("SELECT p FROM Payment p WHERE p.sale.id = :saleId")
    List<Payment> findBySaleId(@Param("saleId") Long saleId);

    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);

    List<Payment> findByCurrency(Currency currency);

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "AND p.deleted = false")
    List<Payment> findByPaymentDateBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(p.amountInXOF) FROM Payment p " +
            "WHERE p.paymentMethod = :paymentMethod " +
            "AND p.paymentDate BETWEEN :startDate AND :endDate " +
            "AND p.deleted = false")
    BigDecimal getTotalByPaymentMethodBetween(@Param("paymentMethod") PaymentMethod paymentMethod,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.paymentMethod, SUM(p.amountInXOF) FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "AND p.deleted = false GROUP BY p.paymentMethod")
    List<Object[]> getRevenueByPaymentMethodBetween(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.verified = false AND p.deleted = false")
    List<Payment> findUnverifiedPayments();
}