package com.nexus.sion.feature.member.command.application.service;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.domain.repository.FreelancerRepository;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.member.query.dto.response.FreelancerDetailResponse;
import com.nexus.sion.feature.member.query.repository.FreelancerQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FreelancerCommandService {

  private final FreelancerQueryRepository freelancerQueryRepository;
  private final MemberRepository memberRepository;
  private final FreelancerRepository freelancerRepository;
  private final PasswordEncoder passwordEncoder;

  public void registerFreelancerAsMember(String freelancerId) {
    FreelancerDetailResponse freelancer =
        freelancerQueryRepository.getFreelancerDetail(freelancerId);

    String rawPassword =
        (freelancer.birthday() != null)
            ? String.format(
                "%02d%02d%02d",
                freelancer.birthday().getYear() % 100,
                freelancer.birthday().getMonthValue(),
                freelancer.birthday().getDayOfMonth())
            : "000000";

    String encodedPassword = passwordEncoder.encode(rawPassword);

    Member member =
        Member.builder()
            .employeeIdentificationNumber(freelancer.freelancerId())
            .employeeName(freelancer.name())
            .password(encodedPassword)
            .profileImageUrl(freelancer.profileImageUrl())
            .phoneNumber(freelancer.phoneNumber())
            .email(freelancer.email())
            .birthday(freelancer.birthday())
            .careerYears(freelancer.careerYears())
            .joinedAt(LocalDate.now())
            .role(MemberRole.OUTSIDER)
            .status(MemberStatus.AVAILABLE)
            .build();

    memberRepository.save(member);
    freelancerRepository.deleteById(freelancer.freelancerId());
  }
}
