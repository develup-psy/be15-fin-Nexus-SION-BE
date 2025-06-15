package com.nexus.sion.feature.member.command.application.service;


import com.nexus.sion.feature.member.command.application.dto.response.TokenResponse;
import com.nexus.sion.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberAuthServiceImpl implements MemberService {
    private final JwtTokenProvider jwtTokenProvider;

    /* 테스트 로그인  */
    @Transactional
    public TokenResponse testLogin() {

        // 토큰 발급
        String accessToken = jwtTokenProvider.createToken(1);
        String refreshToken = jwtTokenProvider.createRefreshToken(1);

        return TokenResponse
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
