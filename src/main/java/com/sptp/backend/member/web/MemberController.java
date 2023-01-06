package com.sptp.backend.member.web;

import com.sptp.backend.aws.service.AwsService;
import com.sptp.backend.jwt.service.dto.CustomUserDetails;
import com.sptp.backend.member.web.dto.request.*;
import com.sptp.backend.jwt.service.JwtService;
import com.sptp.backend.member.web.dto.response.*;
import com.sptp.backend.member.repository.Member;
import com.sptp.backend.member.service.MemberService;
import com.sptp.backend.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private final JwtService jwtService;

    // 회원가입
    @PostMapping("/members/join")
    public ResponseEntity<MemberSaveResponseDto> join(@RequestBody MemberSaveRequestDto memberSaveRequestDto) {

        Member member = memberService.saveUser(memberSaveRequestDto);

        MemberSaveResponseDto memberSaveResponseDto = MemberSaveResponseDto.builder()
                .nickname(member.getNickname())
                .userId(member.getUserId())
                .email(member.getEmail())
                .telephone(member.getTelephone())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(memberSaveResponseDto);
    }

    // 로그인
    @PostMapping("/members/login")
    public ResponseEntity<?> login(@RequestBody MemberLoginRequestDto memberLoginRequestDto) {

        MemberLoginResponseDto memberLoginResponseDto = memberService.login(memberLoginRequestDto);

        return ResponseEntity.status(HttpStatus.OK).body(memberLoginResponseDto);
    }

    // 토큰 재발급
    @PostMapping("/members/token")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> refreshToken) {

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
    public ResponseEntity<?> findId(@RequestBody MemberFindIdRequestDto memberFindIdRequestDto) throws Exception {

        Member member = memberService.findByEmail(memberFindIdRequestDto);
        emailService.sendIdMessage(member.getEmail());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 비밀번호 임시 발급
    @PostMapping("/members/new-password")
    public ResponseEntity<?> sendNewPassword(@RequestBody Map<String, String> paramMap) throws Exception {

        String email = paramMap.get("email");
        String newPassword = memberService.resetPassword(email);
        emailService.sendNewPasswordMessage(email, newPassword);

        return new ResponseEntity(HttpStatus.OK);
    }

    // 아이디 중복 체크
    @GetMapping("/members/check-id")
    public ResponseEntity<?> checkUserId(@RequestParam("userId") String userId) {

        boolean isDuplicated = memberService.isDuplicateUserId(userId);
        CheckDuplicateResponse checkDuplicateResponse = CheckDuplicateResponse.builder().duplicate(isDuplicated).build();
        return ResponseEntity.status(HttpStatus.OK).body(checkDuplicateResponse);
    }

    // 이메일 중복 체크
    @GetMapping("/members/check-email")
    public ResponseEntity<?> checkUserEmail(@RequestParam("email") String email) {

        boolean isDuplicated = memberService.isDuplicateEmail(email);
        CheckDuplicateResponse checkDuplicateResponse = CheckDuplicateResponse.builder().duplicate(isDuplicated).build();

        return ResponseEntity.status(HttpStatus.OK).body(checkDuplicateResponse);
    }

    // 작가 가입
    @PostMapping("artists/join")
    public ResponseEntity<?> joinAuthor(@RequestParam(value = "image", required = false) MultipartFile image, ArtistSaveRequestDto artistSaveRequestDto) throws IOException {

        Member member = memberService.saveArtist(artistSaveRequestDto, image);

        ArtistSaveResponseDto artistSaveResponseDto = ArtistSaveResponseDto.builder()
                .nickname(member.getNickname())
                .userId(member.getUserId())
                .email(member.getEmail())
                .telephone(member.getTelephone())
                .education(member.getEducation())
                .history(member.getHistory())
                .description(member.getDescription())
                .instagram(member.getInstagram())
                .behance(member.getBehance())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(artistSaveResponseDto);
    }

    // 비밀번호 재설정
    @PatchMapping("/members/password")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid PasswordChangeRequest passwordChangeRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        memberService.changePassword(userDetails.getMember().getId(), passwordChangeRequest.getPassword());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 회원 정보 수정
    @PatchMapping("/members")
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestParam(value = "image", required = false) MultipartFile image,
                                        MemberUpdateRequest memberUpdateRequest) throws IOException {

        memberService.updateUser(userDetails.getMember().getId(), memberUpdateRequest, image);

        return new ResponseEntity(HttpStatus.OK);
    }

    // 회원 탈퇴
    @DeleteMapping("/members")
    public ResponseEntity<?> withdrawUser(
            @RequestHeader("accessToken") String accessToken,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        memberService.logout(accessToken);
        memberService.withdrawUser(userDetails.getMember().getId());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 작가 정보 수정
    @PatchMapping("/artists")
    public ResponseEntity<?> updateArtist(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @RequestParam(value = "image", required = false) MultipartFile image,
                                          ArtistUpdateRequest artistUpdateRequest) throws IOException {

        memberService.updateArtist(userDetails.getMember().getId(), artistUpdateRequest, image);

        return new ResponseEntity(HttpStatus.OK);
    }

    // 회원 정보 조회
    @GetMapping("/members")
    public ResponseEntity<MemberResponse> getMember(@AuthenticationPrincipal CustomUserDetails userDetails){

        Member member = memberService.findById(userDetails.getMember().getId());

        MemberResponse memberResponse = MemberResponse.builder()
                .nickname(member.getNickname())
                .image(member.getImage())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(memberResponse);
    }
}