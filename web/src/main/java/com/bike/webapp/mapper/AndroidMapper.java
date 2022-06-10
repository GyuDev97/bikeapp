package com.bike.webapp.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.bike.webapp.dto.RidingRecord;

@Repository("com.bike.webapp.mapper.AndroidMapper")
public interface AndroidMapper {

	public List<RidingRecord> getRidingRecord(String rr_rider);
}
