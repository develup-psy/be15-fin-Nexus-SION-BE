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

import com.nexus.sion.common.s3.dto.S3UploadResponse;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

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
          new MockMultipartFile("file", "test.jpg", "image/jpg", "test content".getBytes());

      when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
          .thenReturn(PutObjectResponse.builder().build());

      S3UploadResponse response = imageS3Service.uploadFile(file, "prefix");

      assertNotNull(response);
      assertTrue(
          response.getUrl().startsWith("https://" + BUCKET_NAME + ".s3.amazonaws.com/prefix/"));
      assertTrue(response.getSavedFileName().endsWith(".jpg"));
      assertEquals("test.jpg", response.getOriginalFileName());

      verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("정상 업로드 (png)")
    void uploadFileSuccessPng() throws IOException {
      MockMultipartFile file =
          new MockMultipartFile("file", "test.png", "image/png", "test content".getBytes());

      when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
          .thenReturn(PutObjectResponse.builder().build());

      S3UploadResponse response = imageS3Service.uploadFile(file, "prefix");

      assertNotNull(response);
      assertTrue(
          response.getUrl().startsWith("https://" + BUCKET_NAME + ".s3.amazonaws.com/prefix/"));
      assertTrue(response.getSavedFileName().endsWith(".png"));
      assertEquals("test.png", response.getOriginalFileName());

      verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("허용되지 않은 파일 형식")
    void uploadFileInvalidContentType() {
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class, () -> imageS3Service.uploadFile(file, "prefix"));

      assertEquals("허용되지 않은 파일 타입입니다. (허용: jpg, png)", exception.getMessage());
    }

    @Test
    @DisplayName("파일 크기 10MB 초과")
    void uploadFileTooLarge() {
      byte[] largeContent = new byte[10 * 1024 * 1024 + 1];
      MockMultipartFile file =
          new MockMultipartFile("file", "large.jpg", "image/jpg", largeContent);

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class, () -> imageS3Service.uploadFile(file, "prefix"));

      assertEquals("파일 크기가 10MB를 초과합니다.", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("파일 삭제 테스트")
  class DeleteFileTest {

    @Test
    @DisplayName("정상 삭제")
    void deleteFileSuccess() {
      when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
          .thenReturn(DeleteObjectResponse.builder().build());

      assertDoesNotThrow(() -> imageS3Service.deleteFile("prefix", "test.jpg"));

      verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }
  }
}
