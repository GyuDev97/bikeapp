package com.bike.webapp.weather;

import java.time.LocalDateTime;

/** Source of raw KMA responses. Kept as an interface so tests never touch the network. */
public interface KmaWeatherClient {

	/** @return the JSON body of 초단기실황 (getUltraSrtNcst) for the base time, or null on any failure. */
	String fetchObservation(int nx, int ny, LocalDateTime baseDateTime);

	/** @return the JSON body of 초단기예보 (getUltraSrtFcst) for the base time, or null on any failure. */
	String fetchForecast(int nx, int ny, LocalDateTime baseDateTime);
}
