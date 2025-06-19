package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.UserCreateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.repository.UserRepository;
import com.nexus.sion.feature.member.util.PasswordValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public void registerUser(UserCreateRequest request) {
        // 중복 회원 체크 로직
        if (!PasswordValidator.isValid(request.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMAIL);
        }
        if (userRepository.existsByEmployeeIdentificationNumber(request.getEmployeeIdentificationNumber())) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMPLOYEE_IDENTIFICATION_NUMBER);
        }

        Member member = modelMapper.map(request, Member.class);
        member.setEncodedPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(member);
    }
}
