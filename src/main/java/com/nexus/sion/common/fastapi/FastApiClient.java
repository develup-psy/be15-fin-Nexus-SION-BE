package com.nexus.sion.common.fastapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiClient {

    private final RestTemplate restTemplate;

    @Value("${ai.embed-function}")
    private String embedFunctionUrl;

    @Value("${ai.fp-infer}")
    private String fpInferUrl;

    public void sendVectors(List<Map<String, Object>> payloads) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(payloads, headers);

        try {
            restTemplate.postForEntity(embedFunctionUrl, request, String.class);
            log.info("FastAPI 벡터 전송 완료 (총 {}건)", payloads.size());
        } catch (Exception e) {
            log.warn("FastAPI 벡터 전송 실패: {}", e.getMessage());
        }
    }

    public ResponseEntity<String> requestFpInference(String projectId, File pdfFile) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("project_id", projectId);
        body.add("file", new FileSystemResource(pdfFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity(fpInferUrl, request, String.class);
    }
}
