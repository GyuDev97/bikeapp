package com.bike.webapp.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bike.webapp.dto.RidingRecord;
import com.bike.webapp.service.AndroidService;
import com.bike.webapp.service.MemberService;


@RequestMapping("/android")
@Controller
public class AndroidController {

	@Autowired
	AndroidService androidService;
	
	
	// 주행기록 가져오기
	@RequestMapping(value = "get/{rr_rider}")
	public @ResponseBody List<RidingRecord> getRidingRecord(@PathVariable String rr_rider) throws Exception {
		return androidService.readRecord(rr_rider);
	}
	
	
	//GPX파일 불러오기 
	@RequestMapping("/getGpx/rider")
	public String android() {
		return "";
	}
	
	//라이딩 종료 후 GPX파일 업로드
	@RequestMapping(value = "/uploadFileToServer", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> uploadFileToServer(
			@RequestParam("uploadfile") MultipartFile uploadfile) {
		try {
			String filename = uploadfile.getOriginalFilename();
			//경로 리눅스에선 루트경로로 
			String directory = "/home/ec2-user/data/gpx";
			String filepath = Paths.get(directory, filename).toString();
			
			// Save the file locally
			BufferedOutputStream stream =
					new BufferedOutputStream(new FileOutputStream(new File(filepath)));
			stream.write(uploadfile.getBytes());
			stream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	} 
}
