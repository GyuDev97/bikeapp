package com.example.testapplication.dto;

public class AnLogin  {
	public String r_id;
	public String r_pw;
	public String Status;

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	public AnLogin() {}
	
	public AnLogin(String r_id, String r_pw) {
		super();
		this.r_id=r_id;
		this.r_pw=r_pw;
	}
	public String getR_id() {
		return r_id;
	}
	public void setR_id(String r_id) {
		this.r_id = r_id;
	}
	public String getR_pw() {
		return r_pw;
	}
	public void setR_pw(String r_pw) {
		this.r_pw = r_pw;
	}
}