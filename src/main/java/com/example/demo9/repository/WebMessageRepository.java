package com.example.demo9.repository;

import com.example.demo9.entity.WebMessage;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WebMessageRepository extends JpaRepository<WebMessage, Long> {

  List<WebMessage> findByMemberReceiveId_EmailAndReceiveSw(String mid, String receiveSw);


  @Modifying      // 이 쿼리가 데이터를 변경(삭제/수정)함을 JPA에 알림
  @Transactional  // 이 메서드 실행 시 트랜잭션을 시작하도록 함
  void deleteByReceiveSwAndSendSw(String receiveSw, String sendSw);

  @Query("SELECT w FROM WebMessage w WHERE w.memberReceiveId.email = :mid AND (w.receiveSw = 'n' OR w.receiveSw = 'r') ORDER BY w.id DESC")
  Page<WebMessage> findReceivedMessages(@Param("mid") String mid, Pageable pageable);

  @Query("SELECT w FROM WebMessage w WHERE w.memberReceiveId.email = :mid AND (w.receiveSw = 'n') ORDER BY w.id DESC")
  Page<WebMessage> findNewMessages(@Param("mid") String mid, Pageable pageable);

  @Query("SELECT w FROM WebMessage w WHERE w.memberSendId.email = :mid AND (w.sendSw='s') ORDER BY w.id DESC")
  Page<WebMessage> findSendMessages(@Param("mid") String mid, Pageable pageable);

  @Query("SELECT w FROM WebMessage w WHERE w.memberSendId.email = :mid AND (w.receiveSw='n') ORDER BY w.id DESC")
  Page<WebMessage> findReceiveCheckMessages(@Param("mid") String mid, Pageable pageable);

  @Query("SELECT w FROM WebMessage w WHERE (w.memberReceiveId.email = :mid AND w.receiveSw='g') OR (w.memberSendId.email = :mid AND w.sendSw='g') ORDER BY w.id DESC")
  Page<WebMessage> findWasteBasketMessages(@Param("mid") String mid, Pageable pageable);

}
