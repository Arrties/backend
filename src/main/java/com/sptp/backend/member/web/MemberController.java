package com.sptp.backend.member.web;

import com.sptp.backend.member.web.dto.request.MemberFindIdRequestDto;
import com.sptp.backend.jwt.service.JwtService;
import com.sptp.backend.jwt.web.JwtTokenProvider;
import com.sptp.backend.member.web.dto.request.MemberLoginRequestDto;
import com.sptp.backend.member.web.dto.request.MemberSaveRequestDto;
import com.sptp.backend.member.web.dto.response.MemberSaveResponseDto;
import com.sptp.backend.member.web.dto.response.TokenResponseDto;
import com.sptp.backend.member.repository.Member;
import com.sptp.backend.member.service.MemberService;
import com.sptp.backend.email.service.EmailService;
import com.sptp.backend.jwt.web.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private final JwtService jwtService;

    HashMap<String, String> emailMap = new HashMap<>();

    // 회원가입
    @PostMapping("/members/join")
    public ResponseEntity<MemberSaveResponseDto> join(@RequestBody MemberSaveRequestDto memberSaveRequestDto) {

        Member member = memberService.saveUser(memberSaveRequestDto);

        MemberSaveResponseDto memberSaveResponseDto = MemberSaveResponseDto.builder()
                .username(member.getUsername())
                .userId(member.getUserId())
                .email(member.getEmail())
                .address(member.getAddress())
                .telephone(member.getTelephone())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(memberSaveResponseDto);
    }

    // 로그인
    @PostMapping("/members/login")
    public ResponseEntity<?> login(@RequestBody MemberLoginRequestDto memberLoginRequestDto) {

        TokenDto token = memberService.login(memberLoginRequestDto);

        TokenResponseDto tokenResponseDto = TokenResponseDto.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(tokenResponseDto);

    }

    @PostMapping("/members/token")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> refreshToken){

        //Refresh Token 검증
        String recreatedAccessToken = jwtService.validateRefreshToken(refreshToken.get("refreshToken"));

        //Access Token 재발급
        TokenResponseDto tokenResponseDto = TokenResponseDto.builder()
                .accessToken(recreatedAccessToken)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(tokenResponseDto);
    }

    // 로그아웃
    @PostMapping("/members/logout")
    public ResponseEntity logout(@RequestHeader("accessToken") String accessToken) {

        memberService.logout(accessToken);

        return new ResponseEntity(HttpStatus.OK);
    }

    // 아이디 찾기
    @PostMapping("/members/id")
    public ResponseEntity<?> findId(@RequestBody MemberFindIdRequestDto memberFindIdRequestDto) {

        Member member = memberService.findByEmail(memberFindIdRequestDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/emailId?email=" + member.getEmail()));

        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    // 이메일로 아이디 발송
    @PostMapping("/emailId")
    public ResponseEntity emailConfirm(@RequestParam String email) throws Exception {

        emailService.sendMessage(email);
        return new ResponseEntity(HttpStatus.OK);
    }
}
