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

import com.nexus.sion.common.s3.service.DocumentS3Service;

@WebMvcTest(DocumentS3Controller.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentS3ControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private DocumentS3Service documentS3Service;

  @Nested
  @DisplayName("파일 업로드 API 테스트")
  class UploadFileTest {

    @Test
    @DisplayName("업로드 성공")
    void uploadFileSuccess() throws Exception {
      // given
      MockMultipartFile file =
          new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());

      String prefix = "prefix";
      String filename = "test.pdf";
      String s3Url = "https://test-bucket.s3.amazonaws.com/prefix/test.pdf";

      when(documentS3Service.uploadFile(any(), eq(prefix), eq(filename))).thenReturn(s3Url);

      // when & then
      mockMvc
          .perform(
              multipart("/api/document/upload")
                  .file(file)
                  .param("prefix", prefix)
                  .param("filename", filename))
          .andExpect(status().isOk())
          .andExpect(content().string(s3Url));

      verify(documentS3Service).uploadFile(any(), eq(prefix), eq(filename));
    }
  }

  @Nested
  @DisplayName("파일 삭제 API 테스트")
  class DeleteFileTest {

    @Test
    @DisplayName("삭제 성공")
    void deleteFileSuccess() throws Exception {
      String prefix = "prefix";
      String filename = "test.pdf";

      // when & then
      mockMvc
          .perform(
              delete("/api/document/delete").param("prefix", prefix).param("filename", filename))
          .andExpect(status().isOk())
          .andExpect(content().string("삭제 완료"));

      verify(documentS3Service).deleteFile(prefix, filename);
    }
  }
}
