package com.finance.transaction_service.service;

import com.finance.transaction_service.dto.ApiResponse;
import com.finance.transaction_service.entity.Transactions;
import com.finance.transaction_service.exception.BadRequestException;
import com.finance.transaction_service.exception.ResourceNotFoundException;
import com.finance.transaction_service.repository.TransactionsRepository;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

@Service
public class FileService {
    private final MessageSource messageSource;
    private final TransactionsRepository transactionsRepository;

    public FileService(MessageSource messageSource, TransactionsRepository transactionsRepository) {
        this.messageSource = messageSource;
        this.transactionsRepository = transactionsRepository;
    }

    public void fileValidation(MultipartFile file){
        List<String> allowedFile = List.of("image/png", "image/jpeg", "application/pdf");
        if(file.getSize() > 5*1024*1024)
            throw new BadRequestException(messageSource.getMessage("fil.size.error", null, Locale.ENGLISH));
        else if(!allowedFile.contains(file.getContentType()))
            throw new BadRequestException(messageSource.getMessage("fil.content.error", null, Locale.ENGLISH));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Object>> uploadFile(MultipartFile file, Long transactionId) throws IOException {
        try {
            fileValidation(file);
            String filePath = System.getProperty("user.dir") + "/Uploads" + File.separator + file.getOriginalFilename();
            System.out.println("filepath===== "+filePath);

            FileOutputStream fout = new FileOutputStream(filePath);
            fout.write(file.getBytes());
            fout.close();
            System.out.println("traanx=====" + transactionId);

            transactionsRepository.updateFilePathById(filePath,transactionId);

            return ResponseEntity.ok(new ApiResponse<>(
                    HttpStatus.OK,
                    messageSource.getMessage("file.upload.successfully", null, Locale.ENGLISH),
                    null
            ));
        }catch(Exception e){
            System.out.println("e==== " + e);
            throw new RuntimeException(
                    messageSource.getMessage("file.upload.fail", null, Locale.ENGLISH),
                    e
            );
        }
    }

    public ResponseEntity<Resource> downloadFile(Long transactionId) throws FileNotFoundException, MalformedURLException {
        Transactions transactions = transactionsRepository.getReferenceById(transactionId);
        String attachment = transactions.getAttachment();
        Path filePath = Paths.get(attachment);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists())
            throw new ResourceNotFoundException(messageSource.getMessage("file.not.found", null, Locale.ENGLISH));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment + "\"")
                .body(resource);
    }
}
