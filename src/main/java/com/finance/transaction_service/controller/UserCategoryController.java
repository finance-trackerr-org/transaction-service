package com.finance.transaction_service.controller;

import com.finance.transaction_service.dto.ApiResponse;
import com.finance.transaction_service.dto.UserCategoryDto;
import com.finance.transaction_service.dto.UserIdDto;
import com.finance.transaction_service.service.TransactionCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/category")
public class UserCategoryController {
    private final TransactionCategoryService transactionCategoryService;

    public UserCategoryController(TransactionCategoryService transactionCategoryService) {
        this.transactionCategoryService = transactionCategoryService;
    }

    @GetMapping("/get-category")
    ResponseEntity<ApiResponse<Object>> getSystemCategories(@RequestParam(value="userId",required = true) UUID userId){
        return transactionCategoryService.getSystemCategories(userId);
    }

    @PostMapping("/add-category")
    ResponseEntity<ApiResponse<Object>> addUserCategory(@Valid @RequestBody UserCategoryDto userCategoryDto){
        return transactionCategoryService.addUserCategory(userCategoryDto);
    }
}
