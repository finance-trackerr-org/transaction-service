package com.finance.transaction_service.repository;

import com.finance.transaction_service.entity.Transactions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions,Long> {

    @Query("Select t from Transactions t where t.date between :fromDate and :toDate and t.userId=:userId and (:category is NULL or t.category = :category)")
    Page<Transactions> fetchTransactionsBetweenDates(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate, @Param("userId") UUID userId,@Param("category") String category, Pageable pageable);

    @Modifying
    @Transactional
    @Query("Update Transactions t set t.attachment = :filePath where id = :id")
    void updateFilePathById(@Param("filePath") String filePath, @Param("id") Long id);
}
