show tables;

create table webMessage (
  id   int not null auto_increment,/* 메세지 고유번호 */
  title varchar(100) not null,			/* 메세지 제목 */
  content text not null,						/* 메세지 내용 */
  sendId  varchar(20) not null,			/* 보내는 사람 아이디(로그인아이디(메일주소) */
  sendSw  char(1) default 's',			/* 보낸메세지(s), 휴지통(g), 휴지통삭제(x) 표시 */
  sendDate datetime default now(),	/* 메세지 보낸날짜 */
  receiveId  varchar(20) not null,		/* 받는 사람 아이디 */
  receiveSw  char(1) default 'n',			/* 받은메세지(n), 읽은메세지(r), 휴지통(g), 휴지통삭제(x) 표시 */
  receiveDate datetime default now(),	/* 메세지 받은날짜/읽은날짜 */
  primary key(id),
  foreign key(sendId) references member(mid),
  foreign key(receiveId) references member(mid)
);
desc webMessage;

insert into webMessage values (default,'안녕! 말숙아~~','이번주 주말에 뭐하니?','hkd1234',default,default,'kms1234','r',default);
insert into webMessage values (default,'안녕하세요. 관리자님~','메세지가 지연되는것 같은데요?...','hkd1234',default,default,'admin',default,default);
insert into webMessage values (default,'안녕~~~~ 길동아!!','이번주 주말은 바뻐~ 프로젝트 준비해야되~~','kms1234',default,default,'hkd1234','r',default);
insert into webMessage values (default,'말숙아~~수고해~','프로젝트 준비 잘하고..','hkd1234',default,default,'kms1234','n',default);

select * from webMessage;
