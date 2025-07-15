package com.nexus.sion.common.s3.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.UUID;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.sion.common.s3.dto.S3UploadResponse;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@ConditionalOnProperty(name = "cloud.aws.active", havingValue = "true")
@RequiredArgsConstructor
public class DocumentS3Service {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한

  public S3UploadResponse uploadFile(MultipartFile file, String prefix) {
    try {
      String contentType = file.getContentType();
      if (!isAllowedContentType(contentType)) {
        throw new IllegalArgumentException("허용되지 않은 파일 타입입니다. (허용: PDF)");
      }

      if (file.getSize() > MAX_FILE_SIZE) {
        throw new IllegalArgumentException("파일 크기가 10MB를 초과합니다.");
      }

      String originalFilename = file.getOriginalFilename();
      String extension = "";

      int idx = originalFilename.lastIndexOf('.');
      if (idx > 0) {
        extension = originalFilename.substring(idx);
      }

      String uniqueFilename = UUID.randomUUID() + extension;
      String s3Key = prefix + "/" + uniqueFilename;

      s3Client.putObject(
          PutObjectRequest.builder().bucket(bucketName).key(s3Key).contentType(contentType).build(),
          RequestBody.fromBytes(file.getBytes()));

      String url = "https://" + bucketName + ".s3.amazonaws.com/" + s3Key;

      return new S3UploadResponse(url, uniqueFilename, originalFilename);

    } catch (IOException e) {
      throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
    }
  }

  public void deleteFile(String prefix, String filename) {
    try {
      String s3Key = prefix + "/" + filename;
      s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(s3Key).build());
    } catch (S3Exception e) {
      throw new RuntimeException("S3 파일 삭제 중 오류가 발생했습니다: " + e.awsErrorDetails().errorMessage());
    }
  }

  private boolean isAllowedContentType(String contentType) {
    return "application/pdf".equals(contentType);
  }

  public File downloadFileFromUrl(String resumeUrl) {
    try {
      URL url = new URL(resumeUrl);
      URLConnection connection = url.openConnection();

      String contentType = connection.getContentType();
      if (!"application/pdf".equalsIgnoreCase(contentType)) {
        throw new IllegalArgumentException("PDF 파일만 다운로드할 수 있습니다.");
      }
      
      File tempFile = Files.createTempFile("resume_", ".pdf").toFile();
      try (InputStream inputStream = connection.getInputStream();
           FileOutputStream outputStream = new FileOutputStream(tempFile)) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      }

      return tempFile;
    } catch (IOException e) {
      throw new RuntimeException("S3에서 PDF 파일 다운로드 실패: " + e.getMessage(), e);
    }
  }
}
