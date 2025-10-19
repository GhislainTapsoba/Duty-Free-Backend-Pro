package com.djbc.dutyfree.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "receipts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false, unique = true)
    private Sale sale;

    @Column(nullable = false, unique = true, length = 50)
    private String receiptNumber;

    @Column(nullable = false)
    private LocalDateTime printedDate;

    @Column(columnDefinition = "TEXT")
    private String receiptContent;

    @Column(length = 500)
    private String pdfPath;

    @Column(nullable = false)
    private Boolean printed = false;

    @Column(nullable = false)
    private Boolean emailed = false;

    @Column(length = 100)
    private String emailAddress;

    private LocalDateTime emailedDate;

    @Column(length = 500)
    private String headerMessage;

    @Column(length = 500)
    private String footerMessage;
}