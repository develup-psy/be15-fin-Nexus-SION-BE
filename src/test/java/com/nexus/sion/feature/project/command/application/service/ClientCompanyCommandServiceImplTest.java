package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyCreateRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientCompanyCommandServiceImplTest {
    @InjectMocks private ClientCompanyCommandServiceImpl service;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ClientCompanyRepository clientCompanyRepository;


    @Test
    void registerClientCompany_success() {
        // given
        ClientCompanyCreateRequest request = ClientCompanyCreateRequest.builder()
                .companyName("나이스")
                .email("test@example.com")
                .contactNumber("01012345678")
                .build();

        ClientCompany mappedEntity = new ClientCompany();
        when(modelMapper.map(eq(request), eq(ClientCompany.class))).thenReturn(mappedEntity);
        when(clientCompanyRepository.findTopByClientCodeStartingWithOrderByClientCodeDesc("나이".toLowerCase() + "_"))
                .thenReturn(Optional.empty());

        // when
        service.registerClientCompany(request);

        // then
        assertNotNull(mappedEntity.getClientCode());
        verify(clientCompanyRepository).save(mappedEntity);
    }

    @Test
    void registerClientCompany_이메일형식_잘못되었을때_예외발생() {
        // given
        ClientCompanyCreateRequest request = ClientCompanyCreateRequest.builder()
                .companyName("나이스")
                .email("invalid-email")
                .build();

        // when & then
        BusinessException ex = assertThrows(BusinessException.class, () -> service.registerClientCompany(request));
        assertEquals(ErrorCode.INVALID_EMAIL_FORMAT, ex.getErrorCode());
        verify(clientCompanyRepository, never()).save(any());
    }

    @Test
    void registerClientCompany_전화번호형식_잘못되었을때_예외발생() {
        // given
        ClientCompanyCreateRequest request = ClientCompanyCreateRequest.builder()
                .companyName("나이스")
                .email("test@example.com")
                .contactNumber("12345")
                .build();

        // when & then
        BusinessException ex = assertThrows(BusinessException.class, () -> service.registerClientCompany(request));
        assertEquals(ErrorCode.INVALID_PHONE_NUMBER_FORMAT, ex.getErrorCode());
        verify(clientCompanyRepository, never()).save(any());
    }

    @Test
    void generateClientCode_기존코드있을때_숫자증가() {
        // given
        ClientCompanyCreateRequest request = ClientCompanyCreateRequest.builder()
                .companyName("나이스")
                .build();

        String codePrefix = "나이".toLowerCase() + "_";
        ClientCompany existing = new ClientCompany();
        existing.setClientCode("나이_004");

        when(modelMapper.map(any(), eq(ClientCompany.class))).thenReturn(new ClientCompany());
        when(clientCompanyRepository.findTopByClientCodeStartingWithOrderByClientCodeDesc(codePrefix))
                .thenReturn(Optional.of(existing));

        // when
        service.registerClientCompany(request);

        // then
        ArgumentCaptor<ClientCompany> captor = ArgumentCaptor.forClass(ClientCompany.class);
        verify(clientCompanyRepository).save(captor.capture());

        ClientCompany saved = captor.getValue();
        assertEquals("나이_005", saved.getClientCode());
    }

    @Test
    void generateClientCode_숫자파싱실패시_예외() {
        // given
        ClientCompanyCreateRequest request = ClientCompanyCreateRequest.builder()
                .companyName("나이스")
                .build();

        ClientCompany existing = new ClientCompany();
        existing.setClientCode("나이_abc"); // 숫자가 아님

        when(clientCompanyRepository.findTopByClientCodeStartingWithOrderByClientCodeDesc("나이".toLowerCase() + "_"))
                .thenReturn(Optional.of(existing));

        // when & then
        BusinessException ex = assertThrows(BusinessException.class, () -> service.registerClientCompany(request));
        assertEquals(ErrorCode.INVALID_CLIENT_CODE_FORMAT, ex.getErrorCode());
    }
}