package com.bike.webapp.dto;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The {@code weather} object of the record APIs. It is always present: when there is no usable
 * snapshot it is exactly {@code {"available": false}} (never null, never partly filled).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordWeather {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final RecordWeather UNAVAILABLE = new RecordWeather(false, null, null, null, null, null);

	private final boolean available;
	private final String observedAt;
	private final Double temperatureC;
	private final String sky;
	private final String precipitation;
	private final String label;

	private RecordWeather(boolean available, String observedAt, Double temperatureC, String sky,
			String precipitation, String label) {
		this.available = available;
		this.observedAt = observedAt;
		this.temperatureC = temperatureC;
		this.sky = sky;
		this.precipitation = precipitation;
		this.label = label;
	}

	public static RecordWeather unavailable() {
		return UNAVAILABLE;
	}

	/**
	 * @param observedAtKst DATETIME column value; it holds Asia/Seoul wall-clock time
	 * @return an available weather only when the snapshot is complete enough to display
	 */
	public static RecordWeather from(Timestamp observedAtKst, Double temperatureC, String sky,
			String precipitation, String label) {
		if (observedAtKst == null || temperatureC == null || label == null || label.trim().isEmpty()) {
			return UNAVAILABLE;
		}
		String observedAt = observedAtKst.toLocalDateTime().atZone(KST).toOffsetDateTime()
				.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		return new RecordWeather(true, observedAt, temperatureC, sky, precipitation, label.trim());
	}

	public boolean isAvailable() {
		return available;
	}

	public String getObservedAt() {
		return observedAt;
	}

	public Double getTemperatureC() {
		return temperatureC;
	}

	public String getSky() {
		return sky;
	}

	public String getPrecipitation() {
		return precipitation;
	}

	public String getLabel() {
		return label;
	}
}
