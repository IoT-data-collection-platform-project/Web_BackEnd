package com.iot_sw.iot_web_backend.Auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDto {

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해주세요.")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "아이디는 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,12}$",
            message = "비밀번호는 8~12자이며 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String password;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Size(max = 100, message = "이메일은 100자 이하로 입력해주세요.")
    private String email;
}