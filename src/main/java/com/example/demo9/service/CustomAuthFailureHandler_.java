package com.example.demo9.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;

//@Service
public class CustomAuthFailureHandler_ implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
    String errorMessage;

    if (exception instanceof UsernameNotFoundException) {
      // 이메일 자체 없음
      errorMessage = exception.getMessage();

    } else if (exception instanceof BadCredentialsException) {
      // 회원은 존재하지만 비밀번호 틀림
      errorMessage = "비밀번호가 틀렸습니다.";

    } else if (exception instanceof AccountExpiredException) {
      // 탈퇴한 회원
      errorMessage = exception.getMessage();  // "탈퇴한 회원입니다."

    } else {
      // 그 외 오류
      errorMessage = "로그인에 실패했습니다.";
    }

    response.sendRedirect("/member/memberLogin?error=true&message=" + URLEncoder.encode(errorMessage, "UTF-8"));
  }
}
