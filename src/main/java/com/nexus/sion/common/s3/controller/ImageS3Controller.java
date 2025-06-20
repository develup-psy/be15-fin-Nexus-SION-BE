package com.nexus.sion.common.s3.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.sion.common.s3.service.ImageS3Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageS3Controller {

    private final ImageS3Service s3Service;

    // 서버 직접 업로드 방식
    @PostMapping("/upload")
    public String uploadFile(@RequestParam MultipartFile file, @RequestParam String prefix, // 폴더 명
                    @RequestParam String filename) {
        return s3Service.uploadFile(file, prefix, filename);
    }

    // 삭제
    @DeleteMapping("/delete")
    public String deleteFile(@RequestParam String prefix, @RequestParam String filename) {
        s3Service.deleteFile(prefix, filename);
        return "삭제 완료";
    }
}
