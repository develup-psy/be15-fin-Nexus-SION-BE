package com.nexus.sion.infra.logging;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(1) // 가장 먼저 실행되도록
public class MDCLoggingFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    try {
      HttpServletRequest httpRequest = (HttpServletRequest) request;

      // traceId가 없으면 새로 생성
      String traceId = UUID.randomUUID().toString();
      MDC.put("traceId", traceId);
      MDC.put("method", httpRequest.getMethod());
      MDC.put("uri", httpRequest.getRequestURI());

      log.info("[Request] {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

      chain.doFilter(request, response);

    } finally {
      // 요청이 끝나면 반드시 제거
      MDC.clear();
    }
  }
}
