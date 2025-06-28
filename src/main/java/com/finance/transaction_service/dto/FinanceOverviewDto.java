package com.finance.transaction_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinanceOverviewDto {
    @NotNull(message = "User ID cannot be null")
    private UUID userId;

    @NotNull(message = "Date is required")
    @Pattern(
            regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
            message = "Date must be in the format yyyy-MM-dd"
    )
    private String fromDate;

    @NotNull(message = "Date is required")
    @Pattern(
            regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
            message = "Date must be in the format yyyy-MM-dd"
    )
    private String toDate;

    private String category;
}
