package com.bike.webapp.weather;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Parses the data.go.kr 동네예보 JSON and maps KMA codes (SKY, PTY) to the app-facing values.
 * Raw responses never leave this class; callers only get normalised pieces.
 */
public final class KmaResponseParser {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

	/** Observation (초단기실황): temperature and precipitation type at the base time. */
	public static final class Observation {
		public final LocalDateTime observedAt;
		public final double temperatureC;
		public final String precipitationCode;

		Observation(LocalDateTime observedAt, double temperatureC, String precipitationCode) {
			this.observedAt = observedAt;
			this.temperatureC = temperatureC;
			this.precipitationCode = precipitationCode;
		}
	}

	private KmaResponseParser() {
	}

	/** @return null when the response is an error, empty or malformed. */
	public static Observation parseObservation(String json) {
		JsonNode items = items(json);
		if (items == null) {
			return null;
		}
		Double temperature = null;
		String precipitation = null;
		LocalDateTime observedAt = null;
		for (JsonNode item : items) {
			String category = item.path("category").asText("");
			if (observedAt == null) {
				observedAt = stamp(item.path("baseDate").asText(""), item.path("baseTime").asText(""));
			}
			if ("T1H".equals(category)) {
				temperature = number(item.path("obsrValue"));
			} else if ("PTY".equals(category)) {
				precipitation = precipitationFromCode(item.path("obsrValue").asText(""));
			}
		}
		if (observedAt == null || temperature == null || precipitation == null) {
			return null;
		}
		return new Observation(observedAt, temperature, precipitation);
	}

	/**
	 * Picks the SKY forecast slot closest to {@code reference} from a 초단기예보 response.
	 *
	 * @return SKY code mapped to CLEAR / PARTLY_CLOUDY / CLOUDY, or null when unknown.
	 */
	public static String parseSky(String json, LocalDateTime reference) {
		JsonNode items = items(json);
		if (items == null) {
			return null;
		}
		String best = null;
		long bestDistance = Long.MAX_VALUE;
		for (JsonNode item : items) {
			if (!"SKY".equals(item.path("category").asText(""))) {
				continue;
			}
			LocalDateTime slot = stamp(item.path("fcstDate").asText(""), item.path("fcstTime").asText(""));
			String sky = skyFromCode(item.path("fcstValue").asText(""));
			if (slot == null || sky == null) {
				continue;
			}
			long distance = Math.abs(java.time.Duration.between(slot, reference).getSeconds());
			if (distance < bestDistance) {
				bestDistance = distance;
				best = sky;
			}
		}
		return best;
	}

	static String skyFromCode(String code) {
		switch (code.trim()) {
		case "1":
			return WeatherSnapshot.SKY_CLEAR;
		case "3":
			return WeatherSnapshot.SKY_PARTLY_CLOUDY;
		case "4":
			return WeatherSnapshot.SKY_CLOUDY;
		default:
			return null;
		}
	}

	/** PTY: 0 없음, 1 비, 2 비/눈, 3 눈, 4 소나기, 5 빗방울, 6 빗방울눈날림, 7 눈날림. */
	static String precipitationFromCode(String code) {
		switch (code.trim()) {
		case "0":
			return WeatherSnapshot.PRECIP_NONE;
		case "1":
		case "5":
			return WeatherSnapshot.PRECIP_RAIN;
		case "2":
		case "6":
			return WeatherSnapshot.PRECIP_RAIN_SNOW;
		case "3":
		case "7":
			return WeatherSnapshot.PRECIP_SNOW;
		case "4":
			return WeatherSnapshot.PRECIP_SHOWER;
		default:
			return null;
		}
	}

	/** Korean label shown by the apps. Precipitation wins over sky; null when nothing is known. */
	public static String labelFor(String skyCode, String precipitationCode) {
		if (precipitationCode != null) {
			switch (precipitationCode) {
			case WeatherSnapshot.PRECIP_RAIN:
				return "비";
			case WeatherSnapshot.PRECIP_RAIN_SNOW:
				return "비/눈";
			case WeatherSnapshot.PRECIP_SNOW:
				return "눈";
			case WeatherSnapshot.PRECIP_SHOWER:
				return "소나기";
			default:
				break;
			}
		}
		if (skyCode == null) {
			return null;
		}
		switch (skyCode) {
		case WeatherSnapshot.SKY_CLEAR:
			return "맑음";
		case WeatherSnapshot.SKY_PARTLY_CLOUDY:
			return "구름많음";
		case WeatherSnapshot.SKY_CLOUDY:
			return "흐림";
		default:
			return null;
		}
	}

	private static JsonNode items(String json) {
		if (json == null || json.trim().isEmpty()) {
			return null;
		}
		try {
			JsonNode response = MAPPER.readTree(json).path("response");
			if (!"00".equals(response.path("header").path("resultCode").asText(""))) {
				return null;
			}
			JsonNode item = response.path("body").path("items").path("item");
			if (item.isArray()) {
				return item;
			}
			if (item.isObject()) {
				return MAPPER.createArrayNode().add(item);
			}
			return null;
		} catch (IOException | RuntimeException e) {
			return null;
		}
	}

	private static Double number(JsonNode node) {
		try {
			double value = Double.parseDouble(node.asText("").trim());
			// KMA marks missing observations with -999 / 999.
			if (Double.isNaN(value) || Double.isInfinite(value) || Math.abs(value) >= 900.0) {
				return null;
			}
			return value;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static LocalDateTime stamp(String date, String time) {
		if (date.length() != 8 || time.length() != 4) {
			return null;
		}
		try {
			return LocalDateTime.parse(date + time, STAMP);
		} catch (DateTimeParseException e) {
			return null;
		}
	}
}
