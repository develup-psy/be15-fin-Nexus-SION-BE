package com.nexus.sion.common.s3.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import software.amazon.awssdk.services.s3.S3Client;

class DocumentS3ServiceTest {

  @Mock private S3Client s3Client;

  @InjectMocks private DocumentS3Service documentS3Service;

  private static final String BUCKET_NAME = "test-bucket";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    documentS3Service = new DocumentS3Service(s3Client);
    setBucketName(documentS3Service, BUCKET_NAME);
  }

  private void setBucketName(DocumentS3Service service, String bucketName) {
    try {
      var field = DocumentS3Service.class.getDeclaredField("bucketName");
      field.setAccessible(true);
      field.set(service, bucketName);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Nested
  @DisplayName("파일 업로드 테스트")
  class UploadFileTest {

    @Test
    @DisplayName("PDF 업로드 성공")
    void uploadPdfSuccess() throws IOException {
      MockMultipartFile file =
          new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());

      String result = documentS3Service.uploadFile(file, "prefix", "test.pdf");

      assertEquals("https://" + BUCKET_NAME + ".s3.amazonaws.com/prefix/test.pdf", result);
    }

    @Test
    @DisplayName("지원하지 않는 파일 타입 업로드 실패")
    void uploadUnsupportedFileType() {
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> {
                documentS3Service.uploadFile(file, "prefix", "test.txt");
              });

      assertEquals("허용되지 않은 파일 타입입니다. (허용: PDF, Excel)", exception.getMessage());
    }

    @Test
    @DisplayName("파일 크기 10MB 초과")
    void uploadFileTooLarge() {
      byte[] largeContent = new byte[10 * 1024 * 1024 + 1]; // 10MB 초과
      MockMultipartFile file =
          new MockMultipartFile("file", "large.pdf", "application/pdf", largeContent);

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> {
                documentS3Service.uploadFile(file, "prefix", "large.pdf");
              });

      assertEquals("파일 크기가 10MB를 초과합니다.", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("파일 삭제 테스트")
  class DeleteFileTest {

    @Test
    @DisplayName("파일 삭제 성공")
    void deleteFileSuccess() {
      documentS3Service.deleteFile("prefix", "test.pdf");
      // 예외 안 나면 성공으로 간주
    }
  }
}
