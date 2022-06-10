package com.bike.webapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bike.webapp.dto.RidingRecord;
import com.bike.webapp.mapper.AndroidMapper;
import com.bike.webapp.mapper.MemberMapper;

@Service
@Transactional
@ComponentScan(basePackages = {"com.bike.webapp.mapper"})
public class AndroidService {
	
	@Autowired
	private AndroidMapper androidMapper;

	public List<RidingRecord> readRecord(String rr_rider) {
		// TODO Auto-generated method stub
		return androidMapper.getRidingRecord(rr_rider);
	}
}
