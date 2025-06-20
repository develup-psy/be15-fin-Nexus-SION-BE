package com.nexus.sion.common.s3.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.nexus.sion.common.s3.dto.S3UploadResponse;
import com.nexus.sion.common.s3.service.DocumentS3Service;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class ImageS3Controller {

    private final DocumentS3Service s3Service;

    @PostMapping("/upload")
    public S3UploadResponse uploadFile(@RequestParam MultipartFile file, @RequestParam String prefix) {
        return s3Service.uploadFile(file, prefix);
    }

    @DeleteMapping("/delete")
    public String deleteFile(@RequestParam String prefix, @RequestParam String filename) {
        s3Service.deleteFile(prefix, filename);
        return "삭제 완료";
    }
}
