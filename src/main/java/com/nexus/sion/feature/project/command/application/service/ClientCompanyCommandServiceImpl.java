package com.nexus.sion.feature.project.command.application.service;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.util.Validator;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyCreateRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyUpdateRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientCompanyCommandServiceImpl implements ClientCompanyCommandService {

  private final ModelMapper modelMapper;
  private final ClientCompanyRepository clientCompanyRepository;

  @Transactional
  @Override
  public void registerClientCompany(ClientCompanyCreateRequest request) {
    // 기존에 존재하는 고객사는 저장하지 않고 종료(TODO : 프론트에서 처리 필요)
    if (clientCompanyRepository.existsByCompanyName(request.getCompanyName())) {
      throw new BusinessException(ErrorCode.CLIENT_COMPANY_ALREADY_EXIST);
    }

    // 이메일이 있다면 유효성 검사
    if (request.getEmail() != null && !Validator.isEmailValid(request.getEmail())) {
      throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
    }

    // 핸드폰번호가 있다면 유효성 검사
    if (request.getContactNumber() != null
        && !Validator.isPhonenumberValid(request.getContactNumber())) {
      throw new BusinessException(ErrorCode.INVALID_PHONE_NUMBER_FORMAT);
    }

    // 회사 코드 추출
    String clientCode = generateClientCode(request.getCompanyName());

    ClientCompany clientCompany = modelMapper.map(request, ClientCompany.class);
    clientCompany.setClientCode(clientCode);

    clientCompanyRepository.save(clientCompany);
  }

  @Transactional
  @Override
  public void updateClientCompany(ClientCompanyUpdateRequest request, String clientCode) {
    // 기존에 존재하는 고객사인지 확인
    ClientCompany clientCompany =
        clientCompanyRepository
            .findById(clientCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_COMPANY_NOT_FOUND));

    clientCompany.update(request);
  }

  private String generateClientCode(String companyName) {
    String prefix = companyName.substring(0, 2).toLowerCase(); // 접두사
    String codePrefix = prefix + "_";

    // 가장 큰 clientCode 가져오기 (예: na_4)
    String lastClientCode =
        clientCompanyRepository
            .findTopByClientCodeStartingWithOrderByClientCodeDesc(codePrefix)
            .map(ClientCompany::getClientCode)
            .orElse(null);

    int nextNumber = 1;
    if (lastClientCode != null) {
      try {
        // 뒤의 숫자만 파싱
        String numberStr = lastClientCode.substring(codePrefix.length());
        nextNumber = Integer.parseInt(numberStr) + 1;
      } catch (NumberFormatException e) {
        throw new BusinessException(ErrorCode.INVALID_CLIENT_CODE_FORMAT);
      }
    }

    return codePrefix + String.format("%03d", nextNumber);
  }
}
