package com.example.demo9.service;

import com.example.demo9.constant.UserDel;
import com.example.demo9.entity.Member;
import com.example.demo9.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//@Service
@RequiredArgsConstructor
public class CustomUserDetailsService_ implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() ->
                    new UsernameNotFoundException("등록되지 않은 이메일입니다.")
            );

    // 회원은 존재하는데 탈퇴 상태인 경우
    if (member.getUserDel() == UserDel.OK) {
      throw new AccountExpiredException("탈퇴한 회원입니다.");
    }

    return new CustomUserDetails_(member);
  }
}
