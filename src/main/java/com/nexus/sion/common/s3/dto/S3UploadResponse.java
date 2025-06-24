package com.nexus.sion.common.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3UploadResponse {
  private String url; // S3 접근 URL
  private String savedFileName; // S3에 저장된 UUID 파일명
  private String originalFileName; // 원본 파일명
}
