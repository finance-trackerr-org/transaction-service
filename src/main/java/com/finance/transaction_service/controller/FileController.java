package com.finance.transaction_service.controller;

import com.finance.transaction_service.dto.ApiResponse;
import com.finance.transaction_service.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/file")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<ApiResponse<Object>> uploadFile(
            @RequestParam(value="id") Long transactionId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return fileService.uploadFile(file,transactionId);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws MalformedURLException, FileNotFoundException {
        return fileService.downloadFile(id);
    }
}
