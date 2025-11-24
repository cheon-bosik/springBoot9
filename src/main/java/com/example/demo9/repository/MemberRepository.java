package com.example.demo9.repository;

import com.example.demo9.constant.UserDel;
import com.example.demo9.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByEmail(String email);

  Optional<Object> findByEmailAndUserDel(String email, UserDel userDel);

  List<Member> findByUserDel(UserDel userDel);
}
