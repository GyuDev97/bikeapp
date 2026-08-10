package com.bike.trailgo.dto;

public class Server {
    private String IP = "http://gyudrive.ipdisk.co.kr:8080/bike";

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        if (IP != null && IP.trim().length() > 0) {
            this.IP = IP;
        }
    }
}
