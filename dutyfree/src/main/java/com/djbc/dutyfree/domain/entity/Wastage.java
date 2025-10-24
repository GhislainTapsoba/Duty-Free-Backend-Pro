package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "wastages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wastage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "wastage_date", nullable = false)
    private LocalDate wastageDate;

    @Column(name = "reason", nullable = false, length = 50)
    private String reason; // EXPIRED, DAMAGED, THEFT, OTHER

    @Column(length = 500)
    private String description;

    @Column(name = "reported_by", length = 100)
    private String reportedBy;

    @Column(name = "value_lost", precision = 19, scale = 2)
    private java.math.BigDecimal valueLost;

    @Column(nullable = false)
    private Boolean approved = false;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approval_date")
    private LocalDate approvalDate;
}