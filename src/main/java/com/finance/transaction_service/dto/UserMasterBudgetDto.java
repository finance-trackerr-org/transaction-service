package com.finance.transaction_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserMasterBudgetDto {
    @NotNull(message = "User ID cannot be null")
    private UUID userId;

    @NotNull(message = "Date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero")
    private BigDecimal totalBalance;

    private Map<
            @NotBlank(message = "Category name must not be blank") String,
            @PositiveOrZero(message = "Amount must be zero or positive") BigDecimal
            > categoryPricing;
}
