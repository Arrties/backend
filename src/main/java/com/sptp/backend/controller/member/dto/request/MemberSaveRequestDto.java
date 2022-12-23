package com.sptp.backend.controller.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberSaveRequestDto {

    private String username;
    private String email;
    private String password;
    private String address;
    private String tel;

}
