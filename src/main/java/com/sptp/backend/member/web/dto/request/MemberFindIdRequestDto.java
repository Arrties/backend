package com.sptp.backend.member.web.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberFindIdRequestDto {

    private String username;
    private String email;
}
