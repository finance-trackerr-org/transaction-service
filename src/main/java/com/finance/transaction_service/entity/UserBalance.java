package com.finance.transaction_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_balance")
public class UserBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id",nullable = false)
    private UUID userId;

    @Column(name = "date",nullable = false)
    private Date date;

    @Column(name="total_balance",precision = 10,scale = 2,nullable = false)
    private BigDecimal balance = BigDecimal.valueOf(0.0);

    @Column(name="total_income",precision = 10,scale = 2,nullable = false)
    private BigDecimal income = BigDecimal.valueOf(0.0);

    @Column(name="total_expense",precision = 10,scale = 2,nullable = false)
    private BigDecimal expense = BigDecimal.valueOf(0.0);

    @Column(name="category_pricing",columnDefinition = "jsonb")
    private String categoryPricing;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}
