package com.nexus.sion.security.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nexus.sion.exception.CustomJwtException;
import com.nexus.sion.exception.ErrorCode;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
  @Value("${jwt.expiration}")
  private Long expiration;

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.refresh-expiration}")
  private Long refreshExpiration;

  private SecretKey secretKey;

  @PostConstruct
  public void init() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    secretKey = Keys.hmacShaKeyFor(keyBytes);
  }

  /* accessToken 생성 메서드 */
  public String createToken(String employeeIdentificationNumber, String role) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);
    return Jwts.builder()
        .subject(employeeIdentificationNumber)
        .claim("role", role)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(secretKey)
        .compact();
  }

  /* refreshToken 생성 메서드 */
  public String createRefreshToken(String employeeIdentificationNumber, String role) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + refreshExpiration);
    return Jwts.builder()
        .subject(employeeIdentificationNumber)
        .claim("role", role)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(secretKey)
        .compact();
  }

  public long getRefreshTokenExpiration() {
    return refreshExpiration;
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      throw new CustomJwtException(ErrorCode.INVALID_JWT);
    } catch (ExpiredJwtException e) {
      throw new CustomJwtException(ErrorCode.EXPIRED_JWT);
    } catch (UnsupportedJwtException e) {
      throw new CustomJwtException(ErrorCode.UNSUPPORTED_JWT);
    } catch (IllegalArgumentException e) {
      throw new CustomJwtException(ErrorCode.EMPTY_JWT);
    }
  }

  public String getEmployeeIdentificationNumberFromJwt(String token) {
    Claims claims =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    return claims.getSubject();
  }
}
