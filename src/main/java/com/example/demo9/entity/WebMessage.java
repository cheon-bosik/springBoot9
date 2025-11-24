package com.example.demo9.entity;

import com.example.demo9.dto.WebMessageDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
public class WebMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "webMessage_id")
  private Long id;

  @Column(length = 100, nullable = false)
  private String title;

  @Lob
  @NotNull
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="sendId", referencedColumnName = "email")
  private Member memberSendId;

  @Column(length = 1)
  @ColumnDefault("'s'")
  private String sendSw;

  @CreatedDate
  private LocalDateTime sendDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="receiveId", referencedColumnName = "email")
  private Member memberReceiveId;

  @Column(length = 1)
  @ColumnDefault("'n'")
  private String receiveSw;

  @CreatedDate
  private LocalDateTime receiveDate;

  //@Transient
  // 비교를 하기 위한 임시컬럼을 하나 만들어준다.
  @ColumnDefault("1")
  private int msgSw;

  //public static WebMessage dtoToEntity(WebMessageDto dto, Member memberSenderEmail, Member memberReceiveEmail) {
  public static WebMessage dtoToEntity(WebMessageDto dto, @NotEmpty() Member memberSenderEmail, @NotEmpty() Member memberReceiveEmail) {
    return WebMessage.builder()
            .id(dto.getId())
            .title(dto.getTitle())
            .content(dto.getContent())
            .memberSendId(memberSenderEmail)
            .sendSw(dto.getSendSw())
            .sendDate(dto.getSendDate())
            .memberReceiveId(memberReceiveEmail)
            .receiveSw(dto.getReceiveSw())
            .receiveDate(dto.getReceiveDate())
            .msgSw(dto.getMsgSw())
            .build();
  }

}
