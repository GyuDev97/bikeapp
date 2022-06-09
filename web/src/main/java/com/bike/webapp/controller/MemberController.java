package com.bike.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bike.webapp.dto.MemberInfo;
import com.bike.webapp.service.MemberService;

public class MemberController {
	@Autowired
	MemberService memberService;
	
	//유저정보 가져오기
	@RequestMapping(value = "getLoginInfo/{LoginId}")
	public @ResponseBody MemberInfo getLoginInfo(@PathVariable String LoginId) throws Exception{
		return memberService.memberInfo(LoginId);
	}
}
