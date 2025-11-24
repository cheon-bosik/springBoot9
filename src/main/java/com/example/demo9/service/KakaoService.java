package com.example.demo9.service;

import com.example.demo9.constant.Role;
import com.example.demo9.constant.UserDel;
import com.example.demo9.dto.KakaoDto;
import com.example.demo9.entity.Member;
import com.example.demo9.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${kakao.client.id}")
  private  String KAKAO_CLIENT_ID;

  @Value("${kakao.client.secret}")
  private String KAKAO_CLIENT_SECRET;

  @Value("${kakao.redirect.url}")
  private String KAKAO_REDIRECT_URL;

  private final static String KAKAO_AUTH_URI = "https://kauth.kakao.com"; // 카카오 계정 인증을 위한 URI

  private final static String KAKAO_API_URI = "https://kapi.kakao.com"; // 카카오 API에 접근할 때 사용되는 URI

  // https://kauth.kakao.com/oauth/authorize?client_id=KAKAO_CLIENT_ID&redirect_uri=KAKAO_REDIRECT_URL&response_type=code
  // 사용자에게 권한부여를 요청하고, 사용자가 해당 요청을 수락하면 권한코드를 발급하는 엔드포인트이다.
  public String getKakaoLogin() {
    return KAKAO_AUTH_URI + "/oauth/authorize"
            + "?client_id=" + KAKAO_CLIENT_ID
            + "&redirect_uri=" + KAKAO_REDIRECT_URL
            + "&response_type=code";
  }

  public KakaoDto getKakaoInfo(String code) throws Exception {
    if(code == null) throw new Exception("Failed get authorization code");  // Kakao 인증 코드가 null인 경우 예외를 발생

    String accessToken = "";

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-type", "application/x-www-form-urlencoded"); // HTTP요청에 사용되는 콘텐츠유형(Content-Type) : 웹 폼 데이터를 서버로 전송하는 데 주로 사용

      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      // Kakao API와의 통신을 위해 필요한 파라메터
      params.add("grant_type"   , "authorization_code");
      params.add("client_id"    , KAKAO_CLIENT_ID);
      params.add("client_secret", KAKAO_CLIENT_SECRET);
      params.add("code"         , code);
      params.add("redirect_uri" , KAKAO_REDIRECT_URL);

      RestTemplate restTemplate = new RestTemplate();
      HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers); // HTTP요청에 필요한 헤더와 바디 생성

      // RestTemplate을 사용하여 서버로 HTTP POST 요청을 보냄 Spring에서 제공하는 템플릿이다. 주로 외부와의 통신때 사용(MultiValueMap객체와 함께사옹)
      // exchange() 메소드로 api를 호출합니다.
      ResponseEntity<String> response = restTemplate.exchange(
              KAKAO_AUTH_URI + "/oauth/token",
              HttpMethod.POST,
              httpEntity,
              String.class // 응답형식
      );

      // JSON 형식의 문자열을 Java 객체로 변환
      JSONParser jsonParser = new JSONParser();
      // HTTP 응답에서 JSON 문자열을 가져와 파싱
      org.json.simple.JSONObject jsonObj = (org.json.simple.JSONObject) jsonParser.parse(response.getBody());

      accessToken = (String) jsonObj.get("access_token");
    } catch (Exception e) {
      throw new Exception("API call failed");
    }
    return getUserInfoWithToken(accessToken);
  }

  private KakaoDto getUserInfoWithToken(String accessToken) throws Exception {
    // 1. 요청을 위한 HttpHeader 생성
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + accessToken);  // Authorization은 인증토큰포함, Bearer토큰은 oauth2.0프로토콜을 사용하여 접근할때 사용함.
    headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

    // 2. 앞에 정의된것들을 HttpEntity에 담아준다.
    // 3. RestTemplate 사용
    RestTemplate rt = new RestTemplate();
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

    // 4. 카카오 사용자 정보 요청
    ResponseEntity<String> response = rt.exchange(
            //"https://kapi.kakao.com/v2/user/me",
            KAKAO_API_URI + "/v2/user/me",
            HttpMethod.POST,
            entity,
            String.class
    );

    // 5. JSON 파싱(Response 데이터 파싱)
    JSONParser jsonParser = new JSONParser();
    JSONObject jsonObj = (JSONObject) jsonParser.parse(response.getBody());
    JSONObject account = (JSONObject) jsonObj.get("kakao_account");
    JSONObject profile = (JSONObject) account.get("profile");

    // 6. 카카오에서 넘어오는 데이터 추출
    Long id = (Long) jsonObj.get("id"); // 여기서는 특별히 사용하지는 않았다.

    String email = null;
    if (account != null) email = (String) account.get("email");
    String nickname = null;
    if (profile != null) nickname = (String) profile.get("nickname");

    // 카카오로그인한 회원일경우, 기존에 가입된 회원이 아니라면 강제로 가입처리한다. 이때 비번은 '1234'로 암호화 해서 가입처리한다.
    Optional<Member> opMember = memberRepository.findByEmail(email);

    Member member = null;

    if(opMember.isPresent()) {
      member = opMember.get(); // 기존회원이 존재하면 그냥 가져온값으로 member에 ㄷㅐ입
    }
    else {
      System.out.println("member저장중..");
      member = Member.builder()
              .name(nickname != null ? nickname : "일반유저")
              .email(email)
              .password(passwordEncoder.encode("1234")) // 임의 패스워드
              .role(Role.USER)
              .userDel(UserDel.NO)
              .build();
    }
    memberRepository.save(member);
    // RequestContextHolder.currentRequestAttributes(): 현재 스레드에 바인딩된 요청 속성을 반환한다.
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

    HttpSession session = request.getSession(); // 현재 스레드에 바인딩된 요청 속성을 반환
    session.setAttribute("member", member);

    // 7. DTO 반환
    return KakaoDto.builder()
            .id(id)
            .email(email)
            .nickName(nickname != null ? nickname : "일반유저")
            .build();
  }
}
