package com.bike.webapp.mapper;

import com.bike.webapp.dto.MemberInfo;
import com.bike.webapp.dto.MemberLogin;

public interface MemberMapper {

	//안드로이드 로그인
	public MemberLogin LoginProcess(MemberLogin login) throws Exception;
		
	//안드로이드 유저 정보 가져오기
	public MemberInfo getLoginInfo(String LoginId) throws Exception;
}
