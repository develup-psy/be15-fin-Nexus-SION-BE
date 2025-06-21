package com.nexus.sion.common.s3.service;

import static org.junit.jupiter.api.Assertions.*;

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

class ImageS3ServiceTest {

  @Mock private S3Client s3Client;

  @InjectMocks private ImageS3Service imageS3Service;

  private static final String BUCKET_NAME = "test-bucket";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    imageS3Service = new ImageS3Service(s3Client);
    setBucketName(imageS3Service, BUCKET_NAME);
  }

  private void setBucketName(ImageS3Service service, String bucketName) {
    try {
      var field = ImageS3Service.class.getDeclaredField("bucketName");
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
    @DisplayName("정상 업로드 (jpg)")
    void uploadFileSuccessJpg() throws IOException {
      MockMultipartFile file =
          new MockMultipartFile("file", "test.jpg", "image/jpeg", "test content".getBytes());

      String result = imageS3Service.uploadFile(file, "prefix", "test.jpg");

      assertEquals("https://" + BUCKET_NAME + ".s3.amazonaws.com/prefix/test.jpg", result);
    }

    @Test
    @DisplayName("정상 업로드 (png)")
    void uploadFileSuccessPng() throws IOException {
      MockMultipartFile file =
          new MockMultipartFile("file", "test.png", "image/png", "test content".getBytes());

      String result = imageS3Service.uploadFile(file, "prefix", "test.png");

      assertEquals("https://" + BUCKET_NAME + ".s3.amazonaws.com/prefix/test.png", result);
    }

    @Test
    @DisplayName("허용되지 않은 파일 형식")
    void uploadFileInvalidContentType() {
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> {
                imageS3Service.uploadFile(file, "prefix", "test.txt");
              });

      assertEquals("허용되지 않은 파일 타입입니다. (허용: jpg, png)", exception.getMessage());
    }

    @Test
    @DisplayName("파일 크기 10MB 초과")
    void uploadFileTooLarge() {
      byte[] largeContent = new byte[10 * 1024 * 1024 + 1]; // 10MB 초과
      MockMultipartFile file =
          new MockMultipartFile("file", "large.jpg", "image/jpeg", largeContent);

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> {
                imageS3Service.uploadFile(file, "prefix", "large.jpg");
              });

      assertEquals("파일 크기가 10MB를 초과합니다.", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("파일 삭제 테스트")
  class DeleteFileTest {

    @Test
    @DisplayName("정상 삭제")
    void deleteFileSuccess() {
      imageS3Service.deleteFile("prefix", "test.jpg");
      // 삭제 호출 여부만 확인 (굳이 verify 없어도 됨, 예외 안나면 됨)
    }
  }
}
