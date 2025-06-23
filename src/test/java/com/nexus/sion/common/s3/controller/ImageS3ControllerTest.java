package com.nexus.sion.common.s3.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.nexus.sion.common.s3.service.ImageS3Service;

@WebMvcTest(ImageS3Controller.class)
@AutoConfigureMockMvc(addFilters = false) // 보안필터 (CSRF 등) 제거
class ImageS3ControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ImageS3Service imageS3Service;

  @Nested
  @DisplayName("파일 업로드 API 테스트")
  class UploadFileTest {

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadFileSuccess() throws Exception {
      MockMultipartFile file =
          new MockMultipartFile("file", "test.jpg", "image/jpeg", "test content".getBytes());

      String prefix = "prefix";
      String filename = "test.jpg";
      String s3Url = "https://test-bucket.s3.amazonaws.com/prefix/test.jpg";

      when(imageS3Service.uploadFile(any(), eq(prefix), eq(filename))).thenReturn(s3Url);

      mockMvc
          .perform(
              multipart("/api/image/upload")
                  .file(file)
                  .param("prefix", prefix)
                  .param("filename", filename))
          .andExpect(status().isOk())
          .andExpect(content().string(s3Url));

      verify(imageS3Service).uploadFile(any(), eq(prefix), eq(filename));
    }
  }

  @Nested
  @DisplayName("파일 삭제 API 테스트")
  class DeleteFileTest {

    @Test
    @DisplayName("이미지 삭제 성공")
    void deleteFileSuccess() throws Exception {
      String prefix = "prefix";
      String filename = "test.jpg";

      mockMvc
          .perform(delete("/api/image/delete").param("prefix", prefix).param("filename", filename))
          .andExpect(status().isOk())
          .andExpect(content().string("삭제 완료"));

      verify(imageS3Service).deleteFile(prefix, filename);
    }
  }
}
