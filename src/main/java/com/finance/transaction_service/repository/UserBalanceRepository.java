package com.finance.transaction_service.repository;

import com.finance.transaction_service.entity.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {
    @Modifying
    @Query("UPDATE UserBalance ub SET ub.balance = :amount WHERE ub.userId = :userId")
    void setUserBalance(@Param("userId") UUID userId, @Param("amount") double amount);

    @Transactional
    @Modifying
    @Query("UPDATE UserBalance ub SET ub.expense = ub.expense + :amount WHERE ub.userId = :userId")
    int updateUserExpense(@Param("userId") UUID userId, @Param("amount") BigDecimal amount);

    @Transactional
    @Modifying
    @Query("UPDATE UserBalance ub SET ub.income = ub.income + :amount WHERE ub.userId = :userId")
    int updateUserIncome(@Param("userId") UUID userId, @Param("amount") BigDecimal amount);

    @Query(value = """
            SELECT * FROM user_balance
            WHERE EXTRACT(MONTH FROM date) = EXTRACT(MONTH FROM CAST(:inputDate AS DATE))
            AND EXTRACT(YEAR FROM date) = EXTRACT(YEAR FROM CAST(:inputDate AS DATE))
            AND user_id = :userId
            """, nativeQuery = true)
    UserBalance findDateBySameMonthAndYear(@Param("inputDate") Date inputDate,@Param("userId") UUID userId);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE user_balance ub SET total_balance = :totalBalance, category_pricing = CAST(:categoryPricingJson AS jsonb) WHERE ub.user_id = :userId
            AND EXTRACT(MONTH FROM date) = EXTRACT(MONTH FROM CAST(:inputDate AS DATE))
            AND EXTRACT(YEAR FROM date) = EXTRACT(YEAR FROM CAST(:inputDate AS DATE))
            """, nativeQuery = true)
    int updateUserBalanceAndCategory(@Param("userId") UUID userId, @Param("totalBalance") BigDecimal totalBalance,@Param("categoryPricingJson") String categoryPricingJson, @Param("inputDate") Date inputDate);
}
