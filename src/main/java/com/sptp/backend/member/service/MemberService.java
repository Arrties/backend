package com.sptp.backend.member.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import com.sptp.backend.member.web.dto.request.*;
import com.sptp.backend.member.repository.Member;
import com.sptp.backend.member.repository.MemberRepository;
import com.sptp.backend.common.exception.CustomException;
import com.sptp.backend.common.exception.ErrorCode;
import com.sptp.backend.jwt.web.JwtTokenProvider;
import com.sptp.backend.jwt.web.dto.TokenDto;
import com.sptp.backend.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtService jwtService;
    private final RedisTemplate redisTemplate;

    @Transactional
    public Member saveUser(MemberSaveRequestDto dto) {

        checkDuplicateMemberUserID(dto.getUserId());
        checkDuplicateMemberEmail(dto.getEmail());

        Member member = Member.builder()
                .username(dto.getUsername())
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .telephone(dto.getTelephone())
                .roles(Collections.singletonList("ROLE_USER"))
                .build();

        memberRepository.save(member);
        return member;
    }

    @Transactional
    public Member saveAuthor(AuthorSaveRequestDto dto) {

        checkDuplicateMemberUserID(dto.getUserId());
        checkDuplicateMemberEmail(dto.getEmail());

        Member member = Member.builder()
                .username(dto.getUsername())
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .telephone(dto.getTelephone())
                .roles(Collections.singletonList("ROLE_AUTHOR"))
                .education(dto.getEducation())
                .history(dto.getHistory())
                .description(dto.getDescription())
                .instagram(dto.getInstagram())
                .behance(dto.getBehance())
                .build();

        memberRepository.save(member);
        return member;

    }


    public TokenDto login(MemberLoginRequestDto dto) {

        // 이메일 및 비밀번호 유효성 체크
        Member findMember = memberRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        if (!passwordEncoder.matches(dto.getPassword(), findMember.getPassword())) {
            throw new CustomException(ErrorCode.NOT_MATCH_PASSWORD);
        }


        TokenDto tokenDto = jwtTokenProvider.createToken(findMember.getUserId(), findMember.getRoles());
        jwtService.saveRefreshToken(tokenDto);

        return tokenDto;
    }

    public Member findByEmail(MemberFindIdRequestDto dto) {

        // 이메일 및 유저이름 유효성 체크
        Member findMember = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_EMAIL));
        if (!dto.getUsername().equals(findMember.getUsername())) {
            throw new CustomException(ErrorCode.NOT_MATCH_USERNAME);
        }

        return findMember;
    }

    @Transactional
    public String resetPassword(String email) {
        final int PASSWORD_LENGTH = 8;

        Member findMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_EMAIL));

        String password = UUID.randomUUID().toString().substring(0, PASSWORD_LENGTH);
        findMember.changePassword(passwordEncoder.encode(password));

        return findMember.getPassword();
    }

    @Transactional
    public void changePassword(Long loginMemberId, String password) {

        Member findMember = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        findMember.changePassword(passwordEncoder.encode(password));
    }

    public void logout(String accessToken) {
        Long expiration = jwtTokenProvider.getExpiration(accessToken);

        redisTemplate.opsForValue()
                .set(accessToken, "blackList", expiration, TimeUnit.MILLISECONDS);
    }

    public void checkDuplicateMemberUserID(String userId) {
        if (memberRepository.existsByUserId(userId)) {
            throw new CustomException(ErrorCode.EXIST_USER_ID);
        }
    }

    public void checkDuplicateMemberEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EXIST_USER_EMAIL);
        }
    }

    public boolean isDuplicateUserId(String userId) {
        return memberRepository.existsByUserId(userId);
    }

    public boolean isDuplicateEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Transactional
    public void updateUser(Long loginMemberId, MemberUpdateRequest dto) {

        Member findMember = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

        if (StringUtils.isNotBlank(dto.getEmail()) && !dto.getEmail().equals(findMember.getEmail())) {
            checkDuplicateMemberEmail(dto.getEmail());
        }

        findMember.updateUser(dto);
    }

    @Transactional
    public void updateArtist(Member member, ArtistUpdateRequest dto) {

        if(StringUtils.isNotBlank(dto.getEmail()) && !dto.getEmail().equals(member.getEmail())) {
            checkDuplicateMemberEmail(dto.getEmail());
            member.setEmail(dto.getEmail());
        }
        if(StringUtils.isNotBlank(dto.getUsername()))
            member.setUsername(dto.getUsername());
        if(StringUtils.isNotBlank(dto.getImage()))
            member.setImage(dto.getImage());
        if(StringUtils.isNotBlank(dto.getEducation()))
            member.setEducation(dto.getEducation());
        if(StringUtils.isNotBlank(dto.getHistory()))
            member.setHistory(dto.getHistory());
        if(StringUtils.isNotBlank(dto.getDescription()))
            member.setDescription(dto.getDescription());
        if(StringUtils.isNotBlank(dto.getInstagram()))
            member.setInstagram(dto.getInstagram());
        if(StringUtils.isNotBlank(dto.getBehance()))
            member.setBehance(dto.getBehance());

        memberRepository.save(member);
    }
}