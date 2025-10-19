package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.LoyaltyCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyCardRepository extends JpaRepository<LoyaltyCard, Long> {

    Optional<LoyaltyCard> findByCardNumber(String cardNumber);

    Optional<LoyaltyCard> findByCustomerId(Long customerId);

    Boolean existsByCardNumber(String cardNumber);

    List<LoyaltyCard> findByActiveTrue();

    List<LoyaltyCard> findByTier(String tier);

    @Query("SELECT lc FROM LoyaltyCard lc WHERE lc.expiryDate <= :date " +
            "AND lc.active = true AND lc.deleted = false")
    List<LoyaltyCard> findExpiringCards(@Param("date") LocalDate date);

    @Query("SELECT lc FROM LoyaltyCard lc WHERE lc.expiryDate < :date " +
            "AND lc.active = true AND lc.deleted = false")
    List<LoyaltyCard> findExpiredCards(@Param("date") LocalDate date);
}