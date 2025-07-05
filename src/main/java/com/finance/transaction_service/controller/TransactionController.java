package com.finance.transaction_service.controller;

import com.finance.transaction_service.dto.ApiResponse;
import com.finance.transaction_service.dto.FinanceOverviewDto;
import com.finance.transaction_service.dto.TransactionDto;
import com.finance.transaction_service.entity.Transactions;
import com.finance.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/add-transaction")
    ResponseEntity<ApiResponse<Object>> addTransactions(
            @Valid @RequestPart("data") TransactionDto transactionDto,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment
    ){
        return transactionService.addTransactions(transactionDto, attachment);
    }

    @PostMapping("/get-transactions")
    ResponseEntity<ApiResponse<Page<Transactions>>> getTransactions(@RequestParam(value="page",required = false,defaultValue = "0") int page,
                                                                    @RequestParam(value="size",required = false,defaultValue = "5") int size,
                                                                    @Valid @RequestBody FinanceOverviewDto financeOverviewDto
                                                   ) throws ParseException {
        return transactionService.getTransactions(page,size,financeOverviewDto);
    }

    @PostMapping("/finance-overview")
    ResponseEntity<ApiResponse<Object>> getFinanceOverview(@Valid @RequestBody FinanceOverviewDto financeOverviewDto) throws ParseException {
        return transactionService.getFinanceOverview(financeOverviewDto);
    }

    @PostMapping("/transactions-by-category")
    ResponseEntity<ApiResponse<Object>> getTransactionsByCategory(@Valid @RequestBody FinanceOverviewDto financeOverviewDto) throws ParseException {
        return transactionService.getTransactionsByCategory(financeOverviewDto);
    }
}
