package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.PaymentTerminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTerminalRepository extends JpaRepository<PaymentTerminal, Long> {

    Optional<PaymentTerminal> findByTerminalId(String terminalId);

    @Query("SELECT pt FROM PaymentTerminal pt WHERE pt.active = true AND pt.deleted = false")
    List<PaymentTerminal> findAllActive();

    @Query("SELECT pt FROM PaymentTerminal pt WHERE pt.status = :status AND pt.active = true AND pt.deleted = false")
    List<PaymentTerminal> findByStatus(@Param("status") PaymentTerminal.TerminalStatus status);

    @Query("SELECT pt FROM PaymentTerminal pt WHERE pt.cashRegister.id = :cashRegisterId AND pt.active = true AND pt.deleted = false")
    List<PaymentTerminal> findByCashRegisterId(@Param("cashRegisterId") Long cashRegisterId);

    @Query("SELECT pt FROM PaymentTerminal pt WHERE pt.terminalType = :terminalType AND pt.active = true AND pt.deleted = false")
    List<PaymentTerminal> findByTerminalType(@Param("terminalType") PaymentTerminal.TerminalType terminalType);

    @Query("SELECT pt FROM PaymentTerminal pt WHERE pt.manufacturer = :manufacturer AND pt.active = true AND pt.deleted = false")
    List<PaymentTerminal> findByManufacturer(@Param("manufacturer") String manufacturer);

    @Query("SELECT COUNT(pt) FROM PaymentTerminal pt WHERE pt.status = 'ONLINE' AND pt.active = true AND pt.deleted = false")
    Long countOnlineTerminals();

    @Query("SELECT pt FROM PaymentTerminal pt " +
           "WHERE pt.status = 'ONLINE' " +
           "AND pt.active = true " +
           "AND pt.deleted = false " +
           "AND (:cashRegisterId IS NULL OR pt.cashRegister.id = :cashRegisterId) " +
           "ORDER BY pt.lastHeartbeat DESC")
    List<PaymentTerminal> findAvailableTerminals(@Param("cashRegisterId") Long cashRegisterId);
}
