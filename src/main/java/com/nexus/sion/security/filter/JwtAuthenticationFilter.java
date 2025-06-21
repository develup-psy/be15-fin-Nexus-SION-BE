package com.nexus.sion.security.filter;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nexus.sion.exception.CustomJwtException;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = getJwtFromRequest(request);

    if (!StringUtils.hasText(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      if (jwtTokenProvider.validateToken(token)) {
        String employeeIdentificationNumber =
            jwtTokenProvider.getEmployeeIdentificationNumberFromJwt(token);

        UserDetails userDetails;
        if (employeeIdentificationNumber.equals("test")) {
          // 테스트 로그인용 설정 추가
          userDetails =
              new org.springframework.security.core.userdetails.User(
                  employeeIdentificationNumber,
                  "test-password",
                  Collections.singleton(new SimpleGrantedAuthority(MemberRole.ADMIN.name())));
        } else {
          userDetails = userDetailsService.loadUserByUsername(employeeIdentificationNumber);
        }

        PreAuthenticatedAuthenticationToken authentication =
            new PreAuthenticatedAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (CustomJwtException e) {
      request.setAttribute("jwtException", e);
    }

    filterChain.doFilter(request, response);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
