package com.iot_sw.iot_web_backend.Auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(max = 20, message = "아이디는 20자 이하로 입력해주세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(max = 100, message = "비밀번호 형식이 올바르지 않습니다.")
    private String password;
}