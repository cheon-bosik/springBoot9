package com.example.demo9.dto;

import com.example.demo9.entity.WebMessage;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebMessageDto {

  private Long id;

  @NotEmpty(message = "제목은 필수입력입니다.")
  @Column(length = 100, nullable = false)
  private String title;

  @NotEmpty(message = "메세지 내용은 필수입력입니다.")
  private String content;

  @NotEmpty(message = "보내는사람 아이디는 필수입력입니다.")
  @Column(length = 20, nullable = false)
  private String sendId;

  private String sendSw;

  private LocalDateTime sendDate;

  @NotEmpty(message = "받는사람 아이디는 필수입력입니다.")
  @Column(length = 20, nullable = false)
  private String receiveId;

  private String receiveSw;

  private LocalDateTime receiveDate;

  private int msgSw;

  public static WebMessageDto entityToDto(Optional<WebMessage> opWebMessage) {
    return WebMessageDto.builder()
            .id(opWebMessage.get().getId())
            .title(opWebMessage.get().getTitle())
            .content(opWebMessage.get().getContent())
            .sendId(opWebMessage.get().getMemberSendId().getEmail())
            .sendSw(opWebMessage.get().getSendSw())
            .sendDate(opWebMessage.get().getSendDate())
            .receiveId(opWebMessage.get().getMemberReceiveId().getEmail())
            .receiveSw(opWebMessage.get().getReceiveSw())
            .receiveDate(opWebMessage.get().getReceiveDate())
            .msgSw(opWebMessage.get().getMsgSw())
            .build();
  }
}
