package com.bike.webapp.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import org.apache.commons.io.IOUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bike.webapp.dto.AnLogin;
import com.bike.webapp.dto.App_RidingRecord;
import com.bike.webapp.dto.Course;
import com.bike.webapp.dto.LoginInfo;
import com.bike.webapp.dto.RidingRecord;
import com.bike.webapp.dto.*;
import com.bike.webapp.service.AndroidService;
import com.bike.webapp.service.MemberService;
import com.bike.webapp.service.*;


@Controller
@RequestMapping(value = "/app/")
public class AndroidController {

	@Autowired
	MemberService memberService;

	@Autowired
	AndroidService androidService;

	@Autowired
	private RidingService ridingService;
	
	// 안드로이드 세션로그인
	@RequestMapping(value = "login")
	public @ResponseBody AnLogin login(@RequestBody AnLogin login) throws Exception {

		AnLogin list = androidService.LoginProcess(login);
		
		if(list.toString() == "[]") {
			list.setStatus("로그인실패");
		}
		else {
			list.setStatus("Ok");
		}
		return list;
	}
	
	//안드로이드 로그인 후 유저 정보 가져오기
	@RequestMapping(value = "getLoginInfo/{LoginId}")
	public @ResponseBody LoginInfo getLoginInfo(@PathVariable String LoginId) throws Exception{
		return null;
	}

	// 주행기록 등록(안드로이드)
	@RequestMapping(value = "riding/upload")
	@ResponseBody
	public Map<String, String> insertriding(HttpServletRequest request) throws Exception {
		RidingRecord record = new RidingRecord();

		// 현재날짜 timestamp
		Date today = new java.util.Date();
		Timestamp timestamp = new Timestamp(today.getTime());

		record.setRr_rider(request.getParameter("rr_rider"));
		record.setRr_date(timestamp);
		record.setRr_distance(Integer.parseInt(request.getParameter("rr_distance")));
		record.setRr_topspeed(Integer.parseInt(request.getParameter("rr_topspeed")));
		record.setRr_avgspeed(Integer.parseInt(request.getParameter("rr_avgspeed")));
		record.setRr_high(Integer.parseInt(request.getParameter("rr_high")));
		record.setRr_gpx(request.getParameter("rr_gpx"));
		record.setRr_open(Integer.parseInt(request.getParameter("rr_open")));
		record.setRr_breaktime(Integer.parseInt(request.getParameter("rr_breaktime")));
		record.setRr_time(Integer.parseInt(request.getParameter("rr_time")));
		record.setRr_area(request.getParameter("rr_area"));
		record.setRr_name(request.getParameter("rr_name"));
		if ( !request.getParameter("rr_comp").equals("null")) {
			record.setRr_comp(Integer.parseInt(request.getParameter("rr_comp")));
			androidService.insertRecordWithComp(record);
		}
		else
			androidService.insertRecord(record);
		// 안드로이드로부터 받은 데이터
		System.out.println("rr_rider " + request.getParameter("rr_rider")); // 회원아이디
		System.out.println("rr_distance " + request.getParameter("rr_distance")); // 오늘날짜
		System.out.println("rr_topspeed " + request.getParameter("rr_topspeed")); // 소요시간
		System.out.println("rr_avgspeed " + request.getParameter("rr_avgspeed")); // 이동거리
		System.out.println("rr_high " + request.getParameter("rr_high")); // 최대속도
		System.out.println("rr_gpx " + request.getParameter("rr_gpx")); // 평균속도
		System.out.println("rr_open " + request.getParameter("rr_open")); // 획득고도
		System.out.println("rr_breaktime " + request.getParameter("rr_breaktime")); // gpx
		System.out.println("rr_time " + request.getParameter("rr_time")); // 공개여부
		System.out.println("rr_area " + request.getParameter("rr_area")); // 획득고도
		System.out.println("rr_name"+request.getParameter("rr_name"));
		System.out.println("rr_comp "+request.getParameter("rr_comp"));

		// 안드로이드에게 전달하는 데이터
		Map<String, String> result = new HashMap<String, String>();
		result.put("data1", request.getParameter("rr_rider"));

		return result;
	}

	// 주행기록 가져오기
	@RequestMapping(value = "get/{rr_rider}")
	public @ResponseBody List<App_RidingRecord> getRidingRecord(@PathVariable String rr_rider) throws Exception {
		return androidService.readRecord(rr_rider);
	}

	// 주행기록 상세보기
	@RequestMapping(value = "get/detail/{rr_num}")
	public @ResponseBody List<RidingRecord> getRidingRecordDetail(@PathVariable String rr_rider,
			@PathVariable String rr_num) throws Exception {
		RidingRecord record = new RidingRecord();
		return androidService.getRidingRecordDetail(rr_rider, rr_num);
	}
	
	//주행기록 상세보기(공개여부설정)
		@RequestMapping(value = "recordset/open")
		public @ResponseBody Map<String, String> RidingOpenSet(HttpServletRequest request) throws Exception {
			RidingRecord record = new RidingRecord();
			record.setRr_num(Integer.parseInt(request.getParameter("rr_num")));
			record.setRr_open(Integer.parseInt(request.getParameter("rr_open")));
			System.out.println(request.getParameter("rr_num")+ " "+request.getParameter("rr_open"));

			androidService.RidingOpenSet(record);
			// 안드로이드에게 전달하는 데이터
			Map<String, String> result = new HashMap<String, String>();
			result.put("data1", "성공했쩡");

			return result;
		}
	

		@RequestMapping(value = "fileUpload/{dir}/{fileName}", method = RequestMethod.POST)
		public String upload(HttpServletRequest request, MultipartFile file1, @PathVariable(value = "dir") String dir,
				@PathVariable(value="fileName") String fileName) {
			try {

				// String path =
				// "/home/ec2-user/apps/mtbcraft/spring/mtbcraft/src/main/resources/static/gpx";
				String path = "/home/ec2-user/data/" + dir;

				if (!file1.isEmpty()) { // 첨부파일이 존재?
					System.out.println(file1.getSize());
					System.out.println(path);
					try {
						// 디렉토리 생성
						if ( !new File(path).exists())
							new File(path).mkdir();
							// 지정된 업로드 경로로 저장됨
							file1.transferTo(new File(path + "/" + fileName));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "redirect:/";
		}

	// 코스 조회
	@RequestMapping(value = "riding/course")
	public @ResponseBody List<App_RidingRecord> getCourse() throws Exception {
		return androidService.getCourse();
	}

	// 코스 조회
	@RequestMapping(value = "riding/course/{c_num}")
	public @ResponseBody List<Course> getCourseItem(@PathVariable(value = "c_num") String c_num) throws Exception {
		System.out.println(c_num);
		return androidService.getCourseItem(c_num);
	}

	// 코스 스크랩하기
	@RequestMapping(value = "riding/coursescrap")
	public @ResponseBody Map<String, String> coursescrap(HttpServletRequest request) throws Exception {
		Scrap_Status scrap = new Scrap_Status();
		scrap.setSs_rnum(Integer.parseInt(request.getParameter("c_num")));
		scrap.setSs_rider(request.getParameter("ss_rider"));
		
		androidService.insertScrap(scrap);

		System.out.println(request.getParameter("c_num"));
		System.out.println(request.getParameter("ss_rider"));
		Map<String, String> result = new HashMap<String, String>();
		result.put("Status", "Ok");

		return result;
	}

	// 사용자 스크랩 코스 조회
	@RequestMapping(value = "riding/scrap/{rr_rider}")
	public @ResponseBody List<Scrap_Status> getScrap(@PathVariable(value = "rr_rider") String rr_rider) throws Exception {
		return androidService.getScrap(rr_rider);
	}

	// 사용자 스크랩 코스 상세보기
	@RequestMapping(value = "riding/scrap/{rr_rider}/{ss_course}", method = RequestMethod.GET)
	public @ResponseBody List<Course> getScrapDetail(@PathVariable(value = "rr_rider") String rr_rider,
			@PathVariable(value = "ss_course") String ss_course) throws Exception {
		return androidService.getScrapDetail(rr_rider, ss_course);
	}
	
	//스크랩 삭제
	@RequestMapping(value = "riding/scrap/del/{ss_rnum}", method = RequestMethod.GET)
	public void delScrap(@PathVariable(value = "ss_rnum") String ss_rnum) throws Exception {
		System.out.println(ss_rnum);
		androidService.delScrap(ss_rnum);
		
	}

	//좋아요
	@RequestMapping(value = "riding/course/like", method = RequestMethod.POST)
	public @ResponseBody  Map<String, String> likeput(HttpServletRequest request) throws Exception {
		System.out.println(request.getParameter("rr_rider") + " " +request.getParameter("rr_num"));
		RidingRecord record = new RidingRecord();
		record.setRr_rider(request.getParameter("rr_rider"));
		record.setRr_num(Integer.parseInt(request.getParameter("rr_num")));
		androidService.insertLike(record);
		Map<String, String> result = new HashMap<String, String>();
		result.put("Status", "Ok");

		return result;
	}
	
	//좋아요 읽기
	@RequestMapping(value = "riding/course/like/{rr_num}", method = RequestMethod.GET)
	public @ResponseBody List<RidingRecord> getLikeCount(@PathVariable(value = "rr_num") int rr_num) throws Exception {
		return androidService.getLikeCount(rr_num);
	}
	
	//좋아요 상태
		@RequestMapping(value = "riding/course/like/{ls_rnum}/{ls_rider}", method = RequestMethod.GET)
		public @ResponseBody List<Like_Status> getLikeStatus(@PathVariable(value = "ls_rnum") int ls_rnum, @PathVariable(value = "ls_rider") String ls_rider) throws Exception {
			return androidService.getLikeStatus(ls_rnum, ls_rider);
		}
		
	// 파일 다운로드
	@RequestMapping(value = "getGPX/{file_dir}/{file_name}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getImageAsResponseEntity( @PathVariable("file_name") String fileName, @PathVariable("file_dir") String fileDir ) throws IOException {
		
	    HttpHeaders headers = new HttpHeaders();
	    byte[] media = null;
	    InputStream in;
	    
	    in = new FileInputStream("/home/ec2-user/data/" + fileDir + "/" +fileName);
	    
	    media = IOUtils.toByteArray(in);
	    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
	    ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(media, headers, HttpStatus.OK);
	    return responseEntity;
	}

	// 웹뷰용 라이딩 기록
	@RequestMapping(value = "getRidingRecord")
	public String getAppRidingRecord() {
		return "riding/appRidingRecord";
	}
	
	// 경쟁전 조회
	@RequestMapping(value = "competition")
	public @ResponseBody List<App_Competition> getCompetition() throws Exception {
		return androidService.getCompetition();
	}
	
	//경쟁전 참가내역 가져오기
	@RequestMapping(value = "competition/{rr_rider}")
	public @ResponseBody List<String> getjoinedComp(@PathVariable(value = "rr_rider") String rr_rider) throws Exception {
		return androidService.getjoinedComp(rr_rider);
	}

	// 경쟁전 코스 정보
	@RequestMapping(value = "getAppCompCourse")
	public String getAppCompCourse() {
		return "entertainment/appCompCourse";
	}
	
	// C_NUM으로 GPX파일 조회
	@RequestMapping(value="/getCompCourse", method = RequestMethod.GET)
	@ResponseBody
	public Gpx getCompCourse(int c_num) throws Exception {
		String gpxFile = androidService.getCompCourse(c_num);
		Gpx gpx = new Gpx();
		makeGpx(gpx, gpxFile);
		return gpx;
	}
	
	private void makeGpx(Gpx gpx, String gpxFile) throws Exception {
		String path = "/home/ec2-user/data/gpx/"+gpxFile;
		File file = new File(path);
		String txt = "";
		FileInputStream fis = new FileInputStream(file); 
		while(true) { 
			int res = fis.read(); 
			if(res<0) { 
				break; 
			}else { 
				txt += ((char)res);
			}
		}
		fis.close();
		gpx.setting(txt);
	}
	

	
	@RequestMapping("getCompClub/{cs_comp}")
	public @ResponseBody List<CompClub> getCompClub( @PathVariable(value = "cs_comp") int cs_comp) throws Exception {
		return androidService.getCompClub(cs_comp);
	}
	
	//코스검색
	@RequestMapping("riding/CourseSearch")
	public String CourseSearch() throws Exception{
		return "android/Android_CourseSearch";
	}
	
	//위험지역 마커
	@RequestMapping("riding/danger")
	public @ResponseBody List<DangerousArea> RidingDanger() throws Exception {
		return androidService.getDanger();
	}

	@RequestMapping("getCompBadge/{comp_badge}")
	public @ResponseBody Badge getCompBadge ( @PathVariable(value = "comp_badge") int comp_badge) throws Exception {
		return androidService.getCompBadge(comp_badge);
	}
	
	@RequestMapping("getCompScore/{comp_num}")
	public @ResponseBody List<CompScore> getCompScore ( @PathVariable("comp_num") int comp_num) throws Exception {
		return androidService.getCompScore(comp_num);
	}
	
	@RequestMapping(value = "/app/updateCompScore", method = RequestMethod.PUT)
	@ResponseBody
	public void updateCompScore(int avg_speed, int rr_comp, int r_club, String LoginId) throws Exception{

		androidService.updateCompScore(avg_speed, rr_comp, r_club, LoginId);
	}
	
	@RequestMapping(value = "getCompAllScore/{comp_num}")
	public @ResponseBody List<Competition_Status> getCompAllScore( @PathVariable(value="comp_num") int comp_num ) throws Exception {
		return androidService.getCompAllScore(comp_num);
	}
	
	@RequestMapping(value = "updateRank", method = RequestMethod.PUT)
	@ResponseBody
	public void updateRank (int rr_comp, int r_club, int score, int rank) throws Exception {
		androidService.updateRank(rr_comp, r_club, score, rank);
	}
	
	@RequestMapping(value = "getMissionStatus/{LoginId}")
	public @ResponseBody List<Mission_Status> getMissionStatus ( @PathVariable(value="LoginId") String LoginId ) throws Exception {
		return androidService.getMissionStatus(LoginId);
	}
	
	@RequestMapping(value = "getNoClearMission/{LoginId}")
	public @ResponseBody List<Mission> getNoClearMission ( @PathVariable(value="LoginId") String LoginId ) throws Exception {
		return androidService.getNoClearMission(LoginId);
	}
	
	@RequestMapping(value = "updateMissionStatus", method = RequestMethod.PUT)
	@ResponseBody
	public void updateMissionStatus ( String LoginId, int typeScore1, int typeScore2 ) throws Exception {
		androidService.updateMissionStatus(LoginId, typeScore1, typeScore2);
	}
	
	@ResponseBody
	public void insertMissionCom ( String LoginId, int mc_mission, Timestamp mc_time ) throws Exception {
		androidService.insertMissionCom(LoginId, mc_mission, mc_time);
	}
	
	@RequestMapping(value="riding/getrecord")
	@ResponseBody
	public  List<RidingRecord>getRecord () throws Exception {
		return androidService.selectRecord();
	}
	
	@RequestMapping(value="riding/taginsert")
	@ResponseBody
	public Map<String, String>  Taginsert(HttpServletRequest request) {

		System.out.println(request.getParameter("rr_num") + " " + request.getParameter("rr_rider") + " " + request.getParameter("address_dong"));

		App_Tag tag = new App_Tag();
		tag.setTs_rnum(Integer.parseInt(request.getParameter("rr_num")));
		tag.setTs_rider(request.getParameter("rr_rider"));
		tag.setTs_tag(request.getParameter("address_dong"));

		androidService.TagInsert(tag);
		// 안드로이드에게 전달하는 데이터
		Map<String, String> result = new HashMap<>();
		result.put("data1", "성공했쩡");
		return result;
	}
	
	@RequestMapping(value = "getAllMission/{LoginId}")
	public @ResponseBody List<App_Mission> getAllMission ( @PathVariable(value="LoginId") String LoginId) throws Exception {
		return androidService.getAllMission(LoginId);
	}
	
	@RequestMapping(value = "getComMission/{LoginId}")
	public @ResponseBody List<String> getComMission ( @PathVariable(value="LoginId") String LoginId) throws Exception {
		return androidService.getComMission(LoginId);
	}
	
	@RequestMapping(value = "getWhenCom/{LoginId}/{m_num}")
	public @ResponseBody Date getWhenCom ( @PathVariable(value="LoginId") String LoginId,
												@PathVariable(value="m_num") int m_num) throws Exception {
		return androidService.getWhenCom(LoginId, m_num);
	}
	
	@RequestMapping(value = "getMisRanking")
	public @ResponseBody List<App_MissionRanking> getMisRanking () throws Exception {
		return androidService.getMisRanking();
	}
	
	@RequestMapping(value = "getCourseReview/{c_num}")
	public @ResponseBody List<App_CourseReview> getCourseRiview (@PathVariable(value="c_num") int c_num) throws Exception{
		return androidService.getCourseRiview(c_num);
	}

	@RequestMapping("riding/course_view/{rr_num}")
	public String course_view(@PathVariable int rr_num, Model model) throws Exception{
		model.addAttribute("rr_num", rr_num);
		return "android/course_view";
	}

	@RequestMapping("riding/course_share/{rr_num}")
	public String course_share(@PathVariable int rr_num, Model model) throws Exception{
		model.addAttribute("rr_num", rr_num);
		return "android/course_share";
	}

	
	//RR_NUM으로 RIDINGRECORD 조회
	@RequestMapping(value="/getRidingRecordByRR_Num/{rr_num}", method = RequestMethod.GET)
	@ResponseBody
	public RidingRecord getRidingRecordByRR_Num(@PathVariable int rr_num, Model model) throws Exception {
		RidingRecord rr = ridingService.getRidingRecordDetail(rr_num);
		int like = ridingService.getRR_Like(rr_num);
		rr.setRr_like(like);
		return rr;
	}

	@RequestMapping(value = "riding/foup")
	// 주소변경예정 @RequestMapping(value = "/android/recordInsert")
	@ResponseBody
	public Map<String, String> insertFollowRiding(HttpServletRequest request) throws Exception {
		RidingRecord record = new RidingRecord();

		// 현재날짜 timestamp
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// formatter.setTimeZone(TimeZone.getTimeZone("GMT+09"));
		Calendar cal = Calendar.getInstance();
		String today = null;
		today = formatter.format(cal.getTime());
		Timestamp ts = Timestamp.valueOf(today);
		record.setRr_rider(request.getParameter("rr_rider"));
		record.setRr_date(ts);
		record.setRr_distance(Integer.parseInt(request.getParameter("rr_distance")));
		record.setRr_topspeed(Integer.parseInt(request.getParameter("rr_topspeed")));
		record.setRr_avgspeed(Integer.parseInt(request.getParameter("rr_avgspeed")));
		record.setRr_high(Integer.parseInt(request.getParameter("rr_high")));
		record.setRr_gpx(request.getParameter("rr_gpx"));
		record.setRr_open(Integer.parseInt(request.getParameter("rr_open")));
		record.setRr_breaktime(Integer.parseInt(request.getParameter("rr_breaktime")));
		record.setRr_time(Integer.parseInt(request.getParameter("rr_time")));
		record.setRr_area(request.getParameter("rr_area"));
		record.setRr_name(request.getParameter("rr_name"));
		record.setRr_foname(request.getParameter("rr_name"));
		// 안드로이드에게 전달하는 데이터
		Map<String, String> result = new HashMap<String, String>();
		result.put("data1", request.getParameter("rr_rider"));
		androidService.foinsert(record);
		androidService.foupdate(record);
		return result;
	}
	
	@RequestMapping(value = "updateCourseReview")
	@ResponseBody
	public void updateCourseReview(HttpServletRequest request) throws Exception {
		App_CourseReview C_review = new App_CourseReview();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String today = null;
		today = formatter.format(cal.getTime());
		Timestamp ts = Timestamp.valueOf(today);

		C_review.setCr_content(request.getParameter("cr_content"));
		C_review.setCr_images(request.getParameter("cr_image"));
		C_review.setCr_num(Integer.parseInt(request.getParameter("cr_num")));
		C_review.setCr_time(ts);
		androidService.updateCourseReview(C_review);
	}

	@RequestMapping(value = "insertCourseReview")
	@ResponseBody
	public void insertCourseReview(HttpServletRequest request) throws Exception {
		App_CourseReview C_review = new App_CourseReview();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String today = null;
		today = formatter.format(cal.getTime());
		Timestamp ts = Timestamp.valueOf(today);

		C_review.setCr_content(request.getParameter("cr_content"));
		C_review.setCr_images(request.getParameter("cr_image"));
		C_review.setCr_time(ts);
		C_review.setCr_rider(request.getParameter("cr_rider"));
		C_review.setCr_rnum(Integer.parseInt(request.getParameter("cr_rnum")));
		androidService.insertCourseReview(C_review);
	}

	@RequestMapping(value = "deleteCourseReview")
	@ResponseBody
	public void deleteCourseReview(HttpServletRequest request) throws Exception {
		String cr_num = "";

		cr_num = request.getParameter("cr_num");

		androidService.deleteCourseReview(cr_num);
	}

	@RequestMapping(value = "sameCheck/{id}")
	@ResponseBody
	public String sameCheck(@PathVariable(value = "id") String id) throws Exception {
		int response;

		response = androidService.sameCheck(id);

		if (response == 0)
			return "ok";
		else
			return "no";
	}
	
	@RequestMapping(value = "insertreg")
	@ResponseBody
	public void insertReg(HttpServletRequest request) throws Exception {
		Member member  = new Member();
		
		member.setR_id(request.getParameter("id"));
		member.setR_pw(request.getParameter("pw"));
		member.setR_name(request.getParameter("name"));
		member.setR_nickname(request.getParameter("nickname"));	
		member.setR_birth(request.getParameter("birth"));
		member.setR_phone(request.getParameter("phone"));
		member.setR_gender(request.getParameter("gender"));
		member.setR_addr(request.getParameter("addr1"));
		member.setR_addr2(request.getParameter("addr2"));
		member.setR_type(request.getParameter("type"));
		member.setR_image(request.getParameter("image"));

		memberService.memberInsert(member);
	}

	// 사용자 등록 위험 지역 조회
	@RequestMapping(value = "danger/{rr_rider}")
	public @ResponseBody List<DangerousArea> getUserDangerousArea(@PathVariable(value="rr_rider") String rr_rider) throws Exception {
		return ridingService.getUserDangerousArea(rr_rider);
	}
	
	@RequestMapping(value = "insertDanger")
	public void insertDanger(@RequestBody DangerousArea d_area) throws Exception{
		androidService.insertDanger(d_area);
	}

	//긴급 위치알림
	@RequestMapping(value = "dangergps/{lat}/{lon}")
	public  String dangerGps(Model model, @PathVariable(value="lat") String lat, @PathVariable(value="lon") String lon) throws Exception {
	model.addAttribute("lat",lat);
	model.addAttribute("lon",lon);
		return "android/helpgps";
	}
}