package com.bike.webapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bike.webapp.dto.MemberInfo;
import com.bike.webapp.dto.MemberLogin;
import com.bike.webapp.mapper.MemberMapper;

@Service
@Transactional
public class MemberService {

	@Autowired
	private MemberMapper memberMapper;
	
	//회원가입
	
	//로그인
	public MemberLogin LoginProcess(MemberLogin login) throws Exception{
		return memberMapper.LoginProcess(login);
	}
	
	//유저정보
	public MemberInfo memberInfo(String member) throws Exception {
		return memberMapper.getLoginInfo(member);
	}
	
}
