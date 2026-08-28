package com.bike.webapp.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bike.webapp.dto.*;
import com.bike.webapp.mapper.*;


@Service
@Transactional
public class AndroidService {

	@Autowired
	private AndroidMapper androidMapper;
	
	//로그인
	public int LoginProcess(AnLogin login) throws Exception{
		return androidMapper.LoginProcess(login);
	}
	
	public LoginInfo getLoginInfo(String LoginId) throws Exception{
		return androidMapper.getLoginInfo(LoginId);
	}
	
	//라이딩기록 저장
	public void insertRecord(RidingRecord record) throws Exception{
		androidMapper.insertRecord(record);
	}
	
	
	//라이딩 기록 전체보기
	public List<RidingRecord> getRidingRecordAll() {
		return androidMapper.getRidingRecordAll();
	}
	
	//라이딩기록 보기
	public List<App_RidingRecord> readRecord(String rr_rider) throws Exception{
		return androidMapper.readRecord(rr_rider);
	}
	
	//주행기록 상세가져오기
	public List<RidingRecord> getRidingRecordDetail(String rr_rider, String rr_num) throws Exception {
		return androidMapper.getRidingRecordDetail(rr_rider, rr_num);
	}
	
	public int RidingOpenSet(RidingRecord record) throws Exception {
		return androidMapper.RidingOpenSet(record);
	}
	
	//코스가져오기
	public List<App_RidingRecord> getCourse() throws Exception{
		return androidMapper.getCourse();
	}
	
	//코스상세보기
	public List<Course> getCourseItem(String c_num) throws Exception{
		return androidMapper.getCourseItem(c_num);
	}
	
	//코스 스크랩
	public String insertScrap(Scrap_Status scrap) throws Exception {
		return androidMapper.insertScrap(scrap);
	}
	
	//스크랩코스 보기
	public List<Scrap_Status> getScrap(String rr_rider) throws Exception {
		return androidMapper.getScrap(rr_rider);
	}
	
	//스크랩코스 상세보기
	public List<Course> getScrapDetail(String rr_rider, String ss_course) throws Exception {
		return androidMapper.getScrapDetail(rr_rider, ss_course);
	}
	public List<Course> getRidingRecordAllItem(String rr_num) throws Exception {
		return androidMapper.getRidingRecordAllItem(rr_num);
	}
	
	//경쟁전가져오기
	public List<App_Competition> getCompetition() throws Exception{
		return androidMapper.getCompetition();
	}
	
	//경쟁전 참가내역 가져오기
	public List<String> getjoinedComp(String rr_rider) throws Exception{
		return androidMapper.getjoinedComp(rr_rider);
	}
	
	public String getCompCourse(int c_num) throws Exception {
		return androidMapper.getCompCourse(c_num);
	}
	
	public List<CompClub> getCompClub(int cs_comp) throws Exception {
		return androidMapper.getCompClub(cs_comp);
	}
	
	public Badge getCompBadge(int comp_badge) throws Exception {
		return androidMapper.getCompBadge(comp_badge);
	}
	
	public List<CompScore> getCompScore(int comp_num) throws Exception {
		return androidMapper.getCompScore(comp_num);
	}
	
	public void delScrap(String ss_rnum) throws Exception {
		System.out.println("서비스"+ss_rnum);
		androidMapper.delScrap(ss_rnum);
	}
	public String likeput(Like_Status likestatus) throws Exception {
		return androidMapper.likeput(likestatus);
		
	}
	public List<DangerousArea> getDanger() throws Exception {
		// TODO Auto-generated method stub
		return androidMapper.getDanger();
	}
	
	//경쟁전 현황 점수 변경
	public void updateCompScore(int avg_speed, int rr_comp, int r_club, String LoginId) throws Exception{
		androidMapper.updateCompScore(avg_speed, rr_comp, r_club, LoginId);
	}
	
	public void updateRank(int rr_comp, int r_club, int score, int rank) throws Exception{
		androidMapper.updateRank(rr_comp, r_club, score, rank);
	}
	
	public List<Mission_Status> getMissionStatus(String LoginId) throws Exception{
		return androidMapper.getMissionStatus(LoginId);
	}
	
	public List<Mission> getNoClearMission(String LoginId) throws Exception{
		return androidMapper.getNoClearMission(LoginId);
	}
	
	public void updateMissionStatus ( String LoginId, int typeScore1, int typeScore2 ) throws Exception{
		androidMapper.updateMissionStatus(LoginId, typeScore1, typeScore2);
	}
	
	public void insertMissionCom ( String LoginId, int mc_mission, Timestamp mc_time ) throws Exception {
		androidMapper.insertMissionCom(LoginId, mc_mission, mc_time);
	}

	public void insertLike(RidingRecord record) {
		androidMapper.insertLike(record);
		
	}

	public List<RidingRecord> getLikeCount(int rr_num) {
		return androidMapper.getLikeCount(rr_num);
	}

	public List<Like_Status> getLikeStatus(int ls_rnum, String ls_rider) {
		// TODO Auto-generated method stub
		return androidMapper.getLikeStatus(ls_rnum, ls_rider);
	}
	
	public List<RidingRecord> selectRecord() {

		return androidMapper.getRecord();
	}

	public void TagInsert(App_Tag tag) {
		androidMapper.Taginsert(tag);

	}
	
	public List<App_Mission> getAllMission(@Param("LoginId") String LoginId) throws Exception{
		return androidMapper.getAllMission(LoginId);
	}
	
	public List<String> getComMission(@Param("LoginId") String LoginId) throws Exception{
		return androidMapper.getComMission(LoginId);
	}
	
	public Date getWhenCom(@Param("LoginId") String LoginId, @Param("m_num") int m_num) throws Exception {
		return androidMapper.getWhenCom(LoginId, m_num);
	}
	
	public List<App_MissionRanking> getMisRanking() throws Exception {
		return androidMapper.getMisRanking();
	}
	
	public List<App_CourseReview> getCourseRiview(@Param("c_num") int c_num) throws Exception{
		return androidMapper.getCourseRiview(c_num);
	}
	
	public void updateCourseReview (App_CourseReview C_review) throws Exception{
		androidMapper.updateCourseReview(C_review);
	}
	
	public void insertCourseReview (App_CourseReview C_review) throws Exception{
		androidMapper.insertCourseReview(C_review);
	}
	
	public void deleteCourseReview(String cr_num) throws Exception{
		androidMapper.deleteCourseReview(cr_num);
	}
	
	public int sameCheck(String LoginId) throws Exception{
		return androidMapper.sameCheck(LoginId);
	}

    public void foinsert(RidingRecord record) {
		androidMapper.foinsert(record);
    }

	public void foupdate(RidingRecord record) {
		androidMapper.foupdate(record);
	}
	
	public void insertDanger(DangerousArea d_area) throws Exception{
		androidMapper.insertDanger(d_area);
	}
}
