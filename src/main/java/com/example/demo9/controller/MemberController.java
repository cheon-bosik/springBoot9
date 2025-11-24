package com.example.demo9.controller;

import com.example.demo9.common.PageVO;
import com.example.demo9.common.Pagination;
import com.example.demo9.constant.UserDel;
import com.example.demo9.dto.KakaoDto;
import com.example.demo9.dto.MemberDto;
import com.example.demo9.entity.KakaoMsg;
import com.example.demo9.entity.Member;
import com.example.demo9.entity.WebMessage;
import com.example.demo9.repository.MemberRepository;
import com.example.demo9.repository.WebMessageRepository;
import com.example.demo9.service.KakaoService;
import com.example.demo9.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

  private final MemberService memberService;
  private final MemberRepository memberRepository;
  private final Pagination pagination;
  private final WebMessageRepository webMessageRepository;
  private final KakaoService kakaoService;

  @Autowired
  PasswordEncoder passwordEncoder;

  @GetMapping("/")
  public String homeGet() {
    return "home";
  }

  @GetMapping("/memberLogin")
  public String memberLoginGet(Model model) {
    // 카카오로그인을 위한 서비스객체에 담아놓은 url정보를 넘겨준다.
    model.addAttribute("kakaoUrl", kakaoService.getKakaoLogin());
    return "member/memberLogin";
  }

  @GetMapping("/memberLoginOk")
  public String memberLoginOkGet(RedirectAttributes rttr,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication,
                                HttpSession session) {
    String email = authentication.getName();

//    String name = memberService.getMemberEmailCheck(email).get().getName();
//    String strLevel = memberService.getMemberEmailCheck(email).get().getRole().toString();
    Optional<Member> opMember = memberService.getMemberEmailCheck(email);

    // 등급 정보 처리
    String strLevel = opMember.get().getRole().toString();
    if(strLevel.equals("ADMIN")) strLevel = "관리자";
    else if(strLevel.equals("OPERATOR")) strLevel = "운영자";
    else if(strLevel.equals("USER")) strLevel = "정회원";

    // Http세션에 필요한 정보 저장
    session.setAttribute("sName", opMember.get().getName());
    session.setAttribute("strLevel", strLevel);

    rttr.addFlashAttribute("message", opMember.get().getName() + "님 로그인 되셨습니다.");

    return "redirect:/member/memberMain";
  }

  @GetMapping("/login/error")
  public String loginErrorGet(RedirectAttributes rttr) {
    rttr.addFlashAttribute("loginErrorMsg", "아이디 또는 비밀번호가 일치하지 않습니다.");
    return "redirect:/member/memberLogin";
  }

  /*
  @GetMapping("/login/error")
  public String loginErrorGet() {
    return "redirect:/message/memberDelOk";
  }
  */

  @GetMapping("/memberLogout")
  public String memberLogoutGet(Authentication authentication,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                //RedirectAttributes rttr,
                                HttpSession session) {
    String name = session.getAttribute("sName").toString();
    if(authentication != null) {
      //rttr.addFlashAttribute("message", name + "님 로그아웃 되었습니다.");
      session.invalidate();
      new SecurityContextLogoutHandler().logout(request, response, authentication);
    }
    //return "redirect:/member/memberLogin";
    //return "redirect:/message/memberLogout?name="+name;
    return "redirect:/message/memberLogout?name="+ URLEncoder.encode(name);
  }

  // 카카오 로그인
  @GetMapping("/kakaoLogin")  // http://localhost:9099/member/kakaoLogin : 리다이렉트경로.
  public String kakaoLoginGet(HttpServletRequest request, HttpSession session) throws Exception {
    KakaoDto kakaoInfo = kakaoService.getKakaoInfo(request.getParameter("code"));

    // HTTP 응답을 생성, OK 응답을 반환(아래는 test용으로 찍어본다.)
    //return ResponseEntity.ok().body(new KakaoMsg("Success", kakaoInfo));
    System.out.println(ResponseEntity.ok().body(new KakaoMsg("Success", kakaoInfo)));

    // 카카오에서 로그인했다는것을 세션에 저장해둔다.
    session.setAttribute("sLogin", "kakao");

    // Spring Security 강제 로그인
    UserDetails userDetails = memberService.loadUserByUsername(kakaoInfo.getEmail());
    UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authToken);
    // SecurityContext가 리다이렉트 후 사라짐 방지를 위해 아래코드 추가
    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext());

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    Optional<Member> opMember = memberRepository.findByEmail(email);

    // 등급 정보 처리
    String strLevel = opMember.get().getRole().toString();
    if(strLevel.equals("ADMIN")) strLevel = "관리자";
    else if(strLevel.equals("OPERATOR")) strLevel = "운영자";
    else if(strLevel.equals("USER")) strLevel = "정회원";

    // Http세션에 필요한 정보 저장
    session.setAttribute("sName", opMember.get().getName());
    session.setAttribute("strLevel", strLevel);

    return "redirect:/member/memberMain";
  }

  // 카카오 로그아웃처리
  @GetMapping("/kakaoLogout")
  public String kakaoLogoutGet(Authentication authentication,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                HttpSession session) {
    if(authentication != null) {
      session.invalidate();
      new SecurityContextLogoutHandler().logout(request, response, authentication);
    }
    return "redirect:/member/memberLogin";
  }

  @GetMapping("/memberJoin")
  public String memberJoinGet(Model model) {
    model.addAttribute("memberDto", new MemberDto());
    return "member/memberJoin";
  }

  @PostMapping("/memberJoin")
  public String memberJoinPost(RedirectAttributes rttr,
                              @Valid MemberDto dto,
                              BindingResult bindingResult) {
    if(bindingResult.hasErrors()) {
      return "member/memberJoin";
    }

    try {
      Member member = Member.dtoToEntity(dto, passwordEncoder);
      Member memberRes = memberService.saveMember(member);
      System.out.println("==> " + memberRes);
      rttr.addFlashAttribute("message", "회원에 가입되었습니다.");
      return "redirect:/member/memberLogin";
    } catch (IllegalStateException e) {
      rttr.addFlashAttribute("message", e.getMessage());
      return "redirect:/member/memberJoin";
    }

  }

  @GetMapping("/memberMain")
  public String memberMainGet(Model model, Authentication authentication) {
    String mid =  authentication.getName();
    List<WebMessage> webMessageList = webMessageRepository.findByMemberReceiveId_EmailAndReceiveSw(mid, "n");
    model.addAttribute("webMessageList", webMessageList);
    model.addAttribute("wmCnt", webMessageList.size());
    return "member/memberMain";
  }

  @GetMapping("/memberList")
  public String memberListGet(Model model, PageVO pageVO) {
    pageVO.setSection("member");
    pageVO = pagination.pagination(pageVO);
    model.addAttribute("pageVO", pageVO);
    return "member/memberList";
  }

  @GetMapping("/memberPwdCheck/{flag}")
  public String memberPwdCheckGet(Model model, @PathVariable String flag) {
    // CSRF Token  처리(AJax에서 post처리시)
    model.addAttribute("userCsrf", true);

    model.addAttribute("flag", flag);
    return "member/memberPwdCheck";
  }

  @ResponseBody
  @PostMapping("/memberPwdCheck")
  public int memberPwdCheckPost(String pwd, String email) {
    Optional<Member> member = memberRepository.findByEmail(email);
    if(passwordEncoder.matches(pwd, member.get().getPassword())) return 1;
    else return 0;
  }

  @PostMapping("/memberPwdChange")
  public String memberPwdChangePost(String email,
                                    @RequestParam(name="newPwd", defaultValue = "", required = false) String pwd) {
    Member member = memberRepository.findByEmail(email).orElseThrow();
    member.setPassword(passwordEncoder.encode(pwd));
    memberRepository.save(member);
    return "redirect:/message/memberPwdChangeOk";
  }

  @GetMapping("/memberUpdate")
  public String memberUpdateGet(Model model, String email, Authentication authentication) {
    Optional<Member> opMember = memberRepository.findByEmail(authentication.getName());
    MemberDto dto = MemberDto.entityToDto(opMember);
    // model.addAttribute("memberDto", dto);
    model.addAttribute("dto", dto);
    return "member/memberUpdate";
  }

  @PostMapping("/memberUpdate")
  public String memberUpdatePost(String name, String address,
                                  Authentication authentication,
                                  //@Valid MemberDto memberDto,
                                  @Valid @ModelAttribute("dto") MemberDto dto, // html문서에서 'memberDto'가 아닌,'dto'로 받을경우..
                                  BindingResult bindingResult) {

    if(bindingResult.hasErrors()) {
      System.out.println("dto : " + dto);
      return "member/memberUpdate";
    }

    String email = authentication.getName();
    Member member = memberRepository.findByEmail(email).orElseThrow();
    member.setName(name);
    member.setAddress(address.trim());
    memberRepository.save(member);
    return "redirect:/message/memberUpdateOk?email="+email;
  }

  @GetMapping("/memberDelete")
  public String memberDeleteGet(Authentication authentication, HttpSession session) {
    String email = authentication.getName();
    Member member = memberRepository.findByEmail(email).orElseThrow();
    member.setUserDel(UserDel.OK);
    memberRepository.save(member);
    session.setAttribute("sName", "회원 탈퇴 되셨습니다. ");
    return "redirect:/member/memberLogout";
  }

}
