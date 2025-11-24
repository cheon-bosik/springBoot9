package com.example.demo9.controller;

import com.example.demo9.common.PageVO;
import com.example.demo9.common.Pagination;
import com.example.demo9.constant.UserDel;
import com.example.demo9.dto.WebMessageDto;
import com.example.demo9.entity.Member;
import com.example.demo9.entity.WebMessage;
import com.example.demo9.repository.MemberRepository;
import com.example.demo9.repository.WebMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/webMessage")
public class WebMessageController {

	private final WebMessageRepository webMessageRepository;
  private final MemberRepository memberRepository;
  private final Pagination pagination;

  //@Transactional
	@GetMapping("/webMessage")
	public String webMessageGet(Model model, Authentication authentication,
			@RequestParam(name="receiveId", defaultValue = "1", required = false) String receiveId,
			@RequestParam(name="msgSw", defaultValue = "1", required = false) int msgSw,
			@RequestParam(name="preSw", defaultValue = "1", required = false) int preSw,
			@RequestParam(name="pag", defaultValue = "0", required = false) int pag,
			@RequestParam(name="pageSize", defaultValue = "8", required = false) int pageSize,
			@RequestParam(name="id", defaultValue = "0", required = false) Long id
    ) {
		String mid =  authentication.getName(); // 로그인한 아이디(이메일)

    PageVO pageVO = new PageVO();

    // 웹메세지에서 사용하는 변수(msgSw) : 앞으로 가야하는 위치 설정 - 0:메세지작성, 1:받은메세지, 2:새메세지, 3:보낸메세지, 4:수신확인, 5:휴지통, 6:메세지내용보기, 9:휴지통비우기)보낸메세지(s), 휴지통(g), 휴지통삭제(x) 표시
		if(msgSw == 0) {		  // 메세지 작성(0)
			List<Member> memberList = memberRepository.findByUserDel(UserDel.NO); // 주소록 가져오기
      pageVO.setMsgSw(0);

      if(!receiveId.contains("@")) receiveId = "";
			model.addAttribute("receiveId", receiveId); // 답장을 쓰기위해 넘어올경우처리..
			model.addAttribute("memberList", memberList);
		}
		else if(msgSw == 6) { // 메세지 내용 보기(receiveId를 'r'로 변경하고, 확인날짜를 받은날짜의 (현재시간)으로 변경하고, 내용보기로 간다.)
      pageVO.setMsgSw(6);

			WebMessage webMessage = webMessageRepository.findById(id).orElseThrow();  // 선택한글(id)을 찾아온다.
			if(!webMessage.getMemberSendId().getEmail().equals(mid) &&  webMessage.getReceiveSw().equals("n")) {   // 받은 편지함의 'n'을 'r'로 변경
        webMessage.setReceiveSw("r");
        webMessage.setReceiveDate(LocalDateTime.now()); // 현재시간으로 받은시간을 변경
        webMessageRepository.save(webMessage);  // @Transactional 을 적으면 save메소드 없어도, setter메소드 적용후 자동 저장된다.
      }
      webMessage.setContent(webMessage.getContent().replace("\n", "<br/>"));
			model.addAttribute("webMessage", webMessage);

      pageVO.setPag(pag);
      pageVO.setPageSize(pageSize);
		}
		else if(msgSw == 9) { // 휴지통 비우기(이곳에서 영구삭제자료는 모두 delete시켜준다.)
      pageVO.setPag(pag);
      pageVO.setPageSize(pageSize);

      List<WebMessage> webMessageList = webMessageRepository.findByMemberReceiveId_EmailAndReceiveSw(mid, "g");
			if(webMessageList.size() != 0) {
        webMessageList.forEach(webMessage -> {
          webMessage.setReceiveSw("x");
          webMessageRepository.save(webMessage);
        });
        webMessageRepository.deleteByReceiveSwAndSendSw("x", "x"); // 만약 sendSw와 receiveSw가 모두 'x'이면 해당 자료는 DELETE 처리한다.
        return "redirect:/message/webMessageResetOk";
			}
			else return "redirect:/message/webMessageEmpty";
		}
		else {	// msgSw가 1~5까지 처리...
      pageVO.setPag(pag);
      pageVO.setPageSize(pageSize);
      pageVO.setSection("webMessage");
      pageVO.setMsgSw(msgSw);

      pageVO = pagination.pagination(pageVO);
		}
    pageVO.setPreSw(preSw);   // 내용을 보거나 지운후 원래 위치로 돌아가기 위한 변수 : preSw
    model.addAttribute("pageVO", pageVO);

		return "webMessage/webMessage";
	}

	// 메세지 저장처리
	@PostMapping("/wmInputOk")
	//public String wmInputOkPost(WebMessage webMessage) {
	public String wmInputOkPost(WebMessageDto dto) {
		Member memberSenderEmail = memberRepository.findByEmail(dto.getSendId()).orElseThrow();
		Member memberReceiveEmail = memberRepository.findByEmail(dto.getReceiveId()).orElseThrow();
		if(memberSenderEmail == null || memberReceiveEmail == null) return "redirect:/message/wmMemberIdNo";
		
    WebMessage webMessage = WebMessage.dtoToEntity(dto, memberSenderEmail, memberReceiveEmail);
    webMessageRepository.save(webMessage);

    return "redirect:/message/wmInputOk";
	}

	// 메세지 삭제처리
	@RequestMapping(value = "/webDeleteCheck", method = RequestMethod.GET)
	public String webMessageDeleteOkGet(Long id, int msgSw, int preSw) {
    WebMessage webMessage = webMessageRepository.findById(id).orElseThrow();

    if(msgSw == 5) webMessage.setReceiveSw("g"); // 휴지통은 'g'(받은메세지에서 휴지통이니깐, receiveSw가 'g'로
    else webMessage.setSendSw("x");              // 영구삭제는 'x'(보낸메세지에서 영구삭제니깐, sendSw가 'x'로
    webMessageRepository.save(webMessage);

    if(msgSw == 5) return "redirect:/message/webMessageDeleteMove?msgSw="+preSw;
    else return "redirect:/message/webMessageDeleteOk?msgSw="+preSw;
	}
	
}
