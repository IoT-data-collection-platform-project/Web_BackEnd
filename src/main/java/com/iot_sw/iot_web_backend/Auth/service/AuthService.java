package com.iot_sw.iot_web_backend.Auth.service;

import com.iot_sw.iot_web_backend.Auth.dto.LoginRequestDto;
import com.iot_sw.iot_web_backend.Auth.dto.SignUpRequestDto;
import com.iot_sw.iot_web_backend.Auth.entity.User;
import com.iot_sw.iot_web_backend.Auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;

    // 회원가입
    public void signup(SignUpRequestDto dto) {
        String username = dto.getUsername().trim();
        String email = dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(CONFLICT, "이미 존재하는 아이디입니다.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(email);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(CONFLICT, "이미 사용 중인 아이디 또는 이메일입니다.");
        }
    }

    // 로그인
    public void login(LoginRequestDto dto, HttpServletRequest request) {
        String username = dto.getUsername().trim();
        String attemptKey = username + ":" + request.getRemoteAddr();

        if (loginAttemptService.isBlocked(attemptKey)) {
            throw new ResponseStatusException(TOO_MANY_REQUESTS, "로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, dto.getPassword())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.getSession(true);
            request.changeSessionId();
            request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            loginAttemptService.clear(attemptKey);
        } catch (AuthenticationException exception) {
            loginAttemptService.recordFailure(attemptKey);
            throw new ResponseStatusException(UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }
}