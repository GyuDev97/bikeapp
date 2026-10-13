package com.bike.webapp.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class KmaResponseParserTest {

	static String ncst(String t1h, String pty) {
		return "{\"response\":{\"header\":{\"resultCode\":\"00\",\"resultMsg\":\"NORMAL_SERVICE\"},"
				+ "\"body\":{\"items\":{\"item\":["
				+ "{\"baseDate\":\"20261010\",\"baseTime\":\"1900\",\"category\":\"PTY\",\"nx\":89,\"ny\":90,\"obsrValue\":\"" + pty + "\"},"
				+ "{\"baseDate\":\"20261010\",\"baseTime\":\"1900\",\"category\":\"T1H\",\"nx\":89,\"ny\":90,\"obsrValue\":\"" + t1h + "\"},"
				+ "{\"baseDate\":\"20261010\",\"baseTime\":\"1900\",\"category\":\"REH\",\"nx\":89,\"ny\":90,\"obsrValue\":\"40\"}"
				+ "]}}}}";
	}

	static String fcst(String sky1900, String sky2000) {
		return "{\"response\":{\"header\":{\"resultCode\":\"00\"},\"body\":{\"items\":{\"item\":["
				+ "{\"baseDate\":\"20261010\",\"baseTime\":\"1830\",\"category\":\"SKY\",\"fcstDate\":\"20261010\",\"fcstTime\":\"1900\",\"fcstValue\":\"" + sky1900 + "\",\"nx\":89,\"ny\":90},"
				+ "{\"baseDate\":\"20261010\",\"baseTime\":\"1830\",\"category\":\"SKY\",\"fcstDate\":\"20261010\",\"fcstTime\":\"2000\",\"fcstValue\":\"" + sky2000 + "\",\"nx\":89,\"ny\":90},"
				+ "{\"baseDate\":\"20261010\",\"baseTime\":\"1830\",\"category\":\"T1H\",\"fcstDate\":\"20261010\",\"fcstTime\":\"1900\",\"fcstValue\":\"20\",\"nx\":89,\"ny\":90}"
				+ "]}}}}";
	}

	@Test
	void parsesObservationTemperatureAndPrecipitation() {
		KmaResponseParser.Observation o = KmaResponseParser.parseObservation(ncst("21.0", "0"));
		assertNotNull(o);
		assertEquals(21.0, o.temperatureC, 0.0);
		assertEquals("NONE", o.precipitationCode);
		assertEquals(LocalDateTime.of(2026, 10, 10, 19, 0), o.observedAt);
	}

	@Test
	void mapsPrecipitationCodesToAppValues() {
		assertEquals("RAIN", KmaResponseParser.parseObservation(ncst("10", "1")).precipitationCode);
		assertEquals("RAIN_SNOW", KmaResponseParser.parseObservation(ncst("1", "2")).precipitationCode);
		assertEquals("SNOW", KmaResponseParser.parseObservation(ncst("-2", "3")).precipitationCode);
		assertEquals("RAIN", KmaResponseParser.parseObservation(ncst("10", "5")).precipitationCode);
		assertEquals("SNOW", KmaResponseParser.parseObservation(ncst("-2", "7")).precipitationCode);
		assertNull(KmaResponseParser.parseObservation(ncst("10", "9")));
	}

	@Test
	void missingTemperatureMarkerIsRejected() {
		assertNull(KmaResponseParser.parseObservation(ncst("-999", "0")));
		assertNull(KmaResponseParser.parseObservation(ncst("abc", "0")));
	}

	@Test
	void errorEmptyAndMalformedResponsesAreRejected() {
		assertNull(KmaResponseParser.parseObservation(null));
		assertNull(KmaResponseParser.parseObservation(""));
		assertNull(KmaResponseParser.parseObservation("<OpenAPI_ServiceResponse>SERVICE_KEY_IS_NOT_REGISTERED</OpenAPI_ServiceResponse>"));
		assertNull(KmaResponseParser.parseObservation("{\"response\":{\"header\":{\"resultCode\":\"30\"}}}"));
		assertNull(KmaResponseParser.parseObservation("{\"response\":{\"header\":{\"resultCode\":\"03\",\"resultMsg\":\"NODATA_ERROR\"}}}"));
	}

	@Test
	void singleItemObjectIsAccepted() {
		String json = "{\"response\":{\"header\":{\"resultCode\":\"00\"},\"body\":{\"items\":{\"item\":"
				+ "{\"baseDate\":\"20261010\",\"baseTime\":\"1900\",\"category\":\"SKY\",\"fcstDate\":\"20261010\",\"fcstTime\":\"1900\",\"fcstValue\":\"1\"}}}}}";
		assertEquals("CLEAR", KmaResponseParser.parseSky(json, LocalDateTime.of(2026, 10, 10, 19, 49)));
	}

	@Test
	void skyUsesTheForecastSlotClosestToTheReferenceTime() {
		String json = fcst("1", "4");
		assertEquals("CLEAR", KmaResponseParser.parseSky(json, LocalDateTime.of(2026, 10, 10, 19, 20)));
		assertEquals("CLOUDY", KmaResponseParser.parseSky(json, LocalDateTime.of(2026, 10, 10, 19, 50)));
		assertEquals("PARTLY_CLOUDY", KmaResponseParser.parseSky(fcst("3", "3"), LocalDateTime.of(2026, 10, 10, 19, 0)));
		assertNull(KmaResponseParser.parseSky(fcst("9", "9"), LocalDateTime.of(2026, 10, 10, 19, 0)));
	}

	@Test
	void labelPrefersPrecipitationOverSky() {
		assertEquals("맑음", KmaResponseParser.labelFor("CLEAR", "NONE"));
		assertEquals("구름많음", KmaResponseParser.labelFor("PARTLY_CLOUDY", "NONE"));
		assertEquals("흐림", KmaResponseParser.labelFor("CLOUDY", "NONE"));
		assertEquals("비", KmaResponseParser.labelFor("CLEAR", "RAIN"));
		assertEquals("눈", KmaResponseParser.labelFor(null, "SNOW"));
		assertEquals("소나기", KmaResponseParser.labelFor("CLOUDY", "SHOWER"));
		assertNull(KmaResponseParser.labelFor(null, "NONE"));
	}
}
