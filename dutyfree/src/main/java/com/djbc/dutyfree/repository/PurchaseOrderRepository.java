package com.djbc.dutyfree.repository;

import com.djbc.dutyfree.domain.entity.PurchaseOrder;
import com.djbc.dutyfree.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    Boolean existsByOrderNumber(String orderNumber);

    List<PurchaseOrder> findByStatus(OrderStatus status);

    List<PurchaseOrder> findBySupplierId(Long supplierId);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.orderDate BETWEEN :startDate AND :endDate " +
            "AND po.deleted = false ORDER BY po.orderDate DESC")
    List<PurchaseOrder> findByOrderDateBetween(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.expectedDeliveryDate <= :date " +
            "AND po.status NOT IN ('RECEIVED', 'CANCELLED') AND po.deleted = false")
    List<PurchaseOrder> findOverdueOrders(@Param("date") LocalDate date);

    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.items WHERE po.id = :id")
    Optional<PurchaseOrder> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.supplier.id = :supplierId " +
            "AND po.status = :status AND po.deleted = false")
    List<PurchaseOrder> findBySupplierAndStatus(@Param("supplierId") Long supplierId,
                                                @Param("status") OrderStatus status);
}