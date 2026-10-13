package com.bike.webapp.weather;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Weather at the moment a ride was saved, already normalised for the apps: no KMA codes leak out.
 * {@code observedAt} is Asia/Seoul wall-clock time.
 */
public final class WeatherSnapshot {

	public static final String SKY_CLEAR = "CLEAR";
	public static final String SKY_PARTLY_CLOUDY = "PARTLY_CLOUDY";
	public static final String SKY_CLOUDY = "CLOUDY";
	public static final String SKY_UNKNOWN = "UNKNOWN";

	public static final String PRECIP_NONE = "NONE";
	public static final String PRECIP_RAIN = "RAIN";
	public static final String PRECIP_RAIN_SNOW = "RAIN_SNOW";
	public static final String PRECIP_SNOW = "SNOW";
	public static final String PRECIP_SHOWER = "SHOWER";

	private final LocalDateTime observedAt;
	private final int nx;
	private final int ny;
	private final double temperatureC;
	private final String skyCode;
	private final String precipitationCode;
	private final String label;

	public WeatherSnapshot(LocalDateTime observedAt, int nx, int ny, double temperatureC,
			String skyCode, String precipitationCode, String label) {
		this.observedAt = observedAt;
		this.nx = nx;
		this.ny = ny;
		this.temperatureC = temperatureC;
		this.skyCode = skyCode;
		this.precipitationCode = precipitationCode;
		this.label = label;
	}

	public LocalDateTime getObservedAt() {
		return observedAt;
	}

	/** Wall-clock value for the DATETIME column (read back the same way, see RecordWeather). */
	public Timestamp getObservedAtTimestamp() {
		return Timestamp.valueOf(observedAt);
	}

	public int getNx() {
		return nx;
	}

	public int getNy() {
		return ny;
	}

	public double getTemperatureC() {
		return temperatureC;
	}

	public String getSkyCode() {
		return skyCode;
	}

	public String getPrecipitationCode() {
		return precipitationCode;
	}

	public String getLabel() {
		return label;
	}

}

