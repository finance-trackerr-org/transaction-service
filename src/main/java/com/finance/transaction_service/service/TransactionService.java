package com.finance.transaction_service.service;

import com.finance.transaction_service.constants.AppConstants;
import com.finance.transaction_service.dto.ApiResponse;
import com.finance.transaction_service.dto.FinanceOverviewDto;
import com.finance.transaction_service.dto.TransactionDto;
import com.finance.transaction_service.entity.Transactions;
import com.finance.transaction_service.entity.UserBalance;
import com.finance.transaction_service.exception.ResourceNotFoundException;
import com.finance.transaction_service.repository.TransactionsRepository;
import com.finance.transaction_service.repository.UserBalanceRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TransactionService {
    private final TransactionsRepository transactionsRepository;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;
    private final UserBalanceRepository userBalanceRepository;
    private final FileService fileService;

    public TransactionService(TransactionsRepository transactionsRepository, ModelMapper modelMapper, MessageSource messageSource, UserBalanceRepository userBalanceRepository, FileService fileService) {
        this.transactionsRepository = transactionsRepository;
        this.modelMapper = modelMapper;
        this.messageSource = messageSource;
        this.userBalanceRepository = userBalanceRepository;
        this.fileService = fileService;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ApiResponse<Object>> addTransactions(TransactionDto transactionDto, MultipartFile file) {
        try {
            System.out.println("file=1111====");
            if(!file.isEmpty()) fileService.fileValidation(file);
            System.out.println("file=====");
            TypeMap<TransactionDto, Transactions> typeMap = modelMapper.getTypeMap(TransactionDto.class, Transactions.class);

            if (typeMap == null) {
                typeMap = modelMapper.createTypeMap(TransactionDto.class, Transactions.class);
                typeMap.addMappings(mapper -> mapper.skip(Transactions::setId));
            }
            Transactions transactions = modelMapper.map(transactionDto, Transactions.class);

            Transactions transaction = transactionsRepository.save(transactions);
            System.out.println("transaction=== "+transaction);
            if(!file.isEmpty())
                fileService.uploadFile(file,transaction.getId());

            Date currentDate = new Date();

            Optional<UserBalance> userBalance = Optional.ofNullable(userBalanceRepository.findDateBySameMonthAndYear(currentDate, transactionDto.getUserId()));
            if (userBalance.isPresent()) {
                int updatedCount;
                if (transactionDto.getType().name().equals(AppConstants.EXPENSE)) {
                    updatedCount = userBalanceRepository.updateUserExpense(transactionDto.getUserId(), transactionDto.getAmount());
                } else {
                    updatedCount = userBalanceRepository.updateUserIncome(transactionDto.getUserId(), transactionDto.getAmount());
                }
                if (updatedCount == 0)
                    throw new RuntimeException(messageSource.getMessage("transaction.saving.error", null, Locale.ENGLISH));
            } else {
                UserBalance userMonthBalance = new UserBalance();
                userMonthBalance.setUserId(transactionDto.getUserId());
                userMonthBalance.setDate(currentDate);
                if (transactionDto.getType().name().equals(AppConstants.EXPENSE))
                    userMonthBalance.setExpense(transactionDto.getAmount());
                else userMonthBalance.setIncome(transactionDto.getAmount());
                userBalanceRepository.save(userMonthBalance);
            }


            ApiResponse<Object> apiResponse = new ApiResponse<>(
                    HttpStatus.OK,
                    messageSource.getMessage("transaction.add.success", null, Locale.ENGLISH),
                    null
            );
            return ResponseEntity.ok(apiResponse);
        }catch (Exception ex){
            System.out.println("ex===== "+ex);
            throw new RuntimeException(
                    messageSource.getMessage("transaction.saving.error", null, Locale.ENGLISH),
                    ex
            );
        }
    }

    public ResponseEntity<ApiResponse<Page<Transactions>>> getTransactions(int page,int size, FinanceOverviewDto financeOverviewDto) throws ParseException {
        Pageable pageable = PageRequest.of(page, size);
        UUID userId = financeOverviewDto.getUserId();

        // Formatting dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(financeOverviewDto.getFromDate());
        Date toDate = sdf.parse(financeOverviewDto.getToDate());

        String category = financeOverviewDto.getCategory();
        if(category == null || category.isEmpty()) category = null;
        System.out.println("category==== "+category);

        Page<Transactions> transactions = transactionsRepository.fetchTransactionsBetweenDates(fromDate,toDate,userId,category,pageable);

        ApiResponse<Page<Transactions>> response = new ApiResponse<>(
                HttpStatus.OK,
                messageSource.getMessage("transaction.fetch.success",null, Locale.getDefault()),
                transactions
        );
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponse<Object>> getFinanceOverview(FinanceOverviewDto financeOverviewDto) throws ParseException {
        UUID userId = financeOverviewDto.getUserId();

        // Formatting user selected date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date inputDate = sdf.parse(financeOverviewDto.getFromDate());

        // Get date with previous month and year
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputDate);
        cal.add(Calendar.MONTH,-1);
        Date previousMonthDate = cal.getTime();

        UserBalance userCurrentBalance = userBalanceRepository.findDateBySameMonthAndYear(inputDate,userId);
        UserBalance userPreviousBalance = userBalanceRepository.findDateBySameMonthAndYear(previousMonthDate,userId);
        System.out.println("userCurrentBalance=====" + userCurrentBalance);
        if(userCurrentBalance == null)
            throw new ResourceNotFoundException(messageSource.getMessage("finance.details.not.found", null, Locale.ENGLISH));

        Map<String, Object> financeOverview = new HashMap<>();
        financeOverview.put(AppConstants.BALANCE.toLowerCase(),userCurrentBalance.getBalance());
        financeOverview.put(AppConstants.EXPENSE.toLowerCase(),userCurrentBalance.getExpense());
        financeOverview.put(AppConstants.INCOME.toLowerCase(),userCurrentBalance.getIncome());
        financeOverview.put(AppConstants.PREVIOUS_BALANCE.toLowerCase(),userPreviousBalance != null ? userPreviousBalance.getBalance() : null);
        financeOverview.put(AppConstants.PREVIOUS_EXPENSE.toLowerCase(),userPreviousBalance != null ? userPreviousBalance.getExpense() : null);
        financeOverview.put(AppConstants.PREVIOUS_INCOME.toLowerCase(),userPreviousBalance != null ? userPreviousBalance.getIncome() : null);


        ApiResponse<Object> response = new ApiResponse<>(
                HttpStatus.OK,
                messageSource.getMessage("finance.fetch.success",null, Locale.getDefault()),
                financeOverview
        );

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponse<Object>> getTransactionsByCategory(FinanceOverviewDto financeOverviewDto) throws ParseException {
        UUID userId = financeOverviewDto.getUserId();

        // Formatting dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fromDate = sdf.parse(financeOverviewDto.getFromDate());
        Date toDate = sdf.parse(financeOverviewDto.getToDate());

        Pageable wholePage = Pageable.unpaged();
        Page<Transactions> transactions = transactionsRepository.fetchTransactionsBetweenDates(fromDate,toDate,userId,null,wholePage);
        if(transactions.isEmpty())
            throw new ResourceNotFoundException(messageSource.getMessage("transaction.not.found", null, Locale.ENGLISH));
        Map<String,List<Map<String, Object>>> categoryTransactions= new HashMap<>();
        Map<String,BigDecimal> categoryAmount= new HashMap<>();
        BigDecimal totalAmountSpent = BigDecimal.valueOf(0.0);

        for(Transactions x:transactions){
            String category = x.getCategory();
            BigDecimal amount = x.getAmount();

            // Create a map to represent a transaction object
            Map<String, Object> transactionDetails = new HashMap<>();
            transactionDetails.put("category", category);
            transactionDetails.put("amount", amount);
            transactionDetails.put("type", x.getType());
            transactionDetails.put("attachment", x.getAttachment());
            transactionDetails.put("date", x.getDate());
            transactionDetails.put("description", x.getDescription());

            categoryTransactions
                    .computeIfAbsent(category, k -> new ArrayList<>())
                    .add(transactionDetails);

            if(x.getType().name().equals(AppConstants.EXPENSE)) {
                totalAmountSpent = totalAmountSpent.add(amount);

                categoryAmount.merge(category, amount, BigDecimal::add);
            }
        }

        Map<String, BigDecimal> categoryPercentage = new HashMap<>();

        for (Map.Entry<String, BigDecimal> entry : categoryAmount.entrySet()) {
            String category = entry.getKey();
            BigDecimal amount = entry.getValue();

            BigDecimal percentage = amount
                    .divide(totalAmountSpent, 2, RoundingMode.HALF_UP)  // 2 decimal places
                    .multiply(BigDecimal.valueOf(100));

            categoryPercentage.put(category, percentage);
        }

        Map<String, Object> categoryTransactionsOverview = new HashMap<>();
        categoryTransactionsOverview.put("categoryTransactions",categoryTransactions);
        categoryTransactionsOverview.put("categoryAmount",categoryAmount);
        categoryTransactionsOverview.put("categoryPercentage",categoryPercentage);

        ApiResponse<Object> response = new ApiResponse<>(
                HttpStatus.OK,
                messageSource.getMessage("category.transactions.fetch.success",null, Locale.getDefault()),
                categoryTransactionsOverview
        );

        return ResponseEntity.ok(response);
    }
}