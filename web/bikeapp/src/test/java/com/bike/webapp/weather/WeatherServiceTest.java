package com.bike.webapp.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class WeatherServiceTest {

	private static final LocalDateTime SAVED_AT = LocalDateTime.of(2026, 10, 10, 19, 49);
	// Daegu 앞산
	private static final double LAT = 35.8329;
	private static final double LON = 128.5650;

	static class FakeClient implements KmaWeatherClient {
		String observation = KmaResponseParserTest.ncst("21.0", "0");
		String forecast = KmaResponseParserTest.fcst("1", "1");
		boolean throwOnCall;
		int observationCalls;
		int forecastCalls;
		LocalDateTime lastObservationBase;
		LocalDateTime lastForecastBase;

		@Override
		public String fetchObservation(int nx, int ny, LocalDateTime base) {
			observationCalls++;
			lastObservationBase = base;
			if (throwOnCall) {
				throw new IllegalStateException("simulated timeout");
			}
			return observation;
		}

		@Override
		public String fetchForecast(int nx, int ny, LocalDateTime base) {
			forecastCalls++;
			lastForecastBase = base;
			if (throwOnCall) {
				throw new IllegalStateException("simulated timeout");
			}
			return forecast;
		}
	}

	private static WeatherService service(FakeClient client, boolean configured) {
		return new WeatherService(client, configured, Clock.fixed(Instant.parse("2026-10-10T10:49:00Z"), WeatherService.KST));
	}

	@Test
	void withoutServiceKeyNothingIsCalledAndNothingFails() {
		FakeClient client = new FakeClient();
		Optional<WeatherSnapshot> result = service(client, false).lookup(LAT, LON, SAVED_AT);
		assertFalse(result.isPresent());
		assertEquals(0, client.observationCalls);
		assertEquals(0, client.forecastCalls);
	}

	@Test
	void withKeyReturnsNormalisedSnapshot() {
		FakeClient client = new FakeClient();
		WeatherSnapshot snapshot = service(client, true).lookup(LAT, LON, SAVED_AT).get();
		assertEquals(21.0, snapshot.getTemperatureC(), 0.0);
		assertEquals("CLEAR", snapshot.getSkyCode());
		assertEquals("NONE", snapshot.getPrecipitationCode());
		assertEquals("맑음", snapshot.getLabel());
		assertEquals(LocalDateTime.of(2026, 10, 10, 19, 0), snapshot.getObservedAt());
		assertEquals(88, snapshot.getNx());
		assertEquals(90, snapshot.getNy());
	}

	@Test
	void usesNearestValidPublishedBaseTimes() {
		FakeClient client = new FakeClient();
		service(client, true).lookup(LAT, LON, SAVED_AT);
		// 19:49 -> observation of 19:00 (available ~19:10), forecast base 19:30 (available ~19:45)
		assertEquals(LocalDateTime.of(2026, 10, 10, 19, 0), client.lastObservationBase);
		assertEquals(LocalDateTime.of(2026, 10, 10, 19, 30), client.lastForecastBase);
		// 19:20: observation 19:00 still fine, but the 19:30 forecast is not out yet -> 18:30
		assertEquals(LocalDateTime.of(2026, 10, 10, 18, 30), WeatherService.forecastBase(LocalDateTime.of(2026, 10, 10, 19, 20)));
		// 19:05: the 19:00 observation is not out yet -> 18:00
		assertEquals(LocalDateTime.of(2026, 10, 10, 18, 0), WeatherService.observationBase(LocalDateTime.of(2026, 10, 10, 19, 5)));
	}

	@Test
	void externalFailureTimeoutOr500YieldsEmptyWithoutThrowing() {
		FakeClient timeout = new FakeClient();
		timeout.throwOnCall = true;
		assertFalse(service(timeout, true).lookup(LAT, LON, SAVED_AT).isPresent());

		FakeClient serverError = new FakeClient();
		serverError.observation = null; // KmaHttpClient returns null for timeouts and HTTP 5xx
		assertFalse(service(serverError, true).lookup(LAT, LON, SAVED_AT).isPresent());
	}

	@Test
	void forecastFailureStillKeepsTemperatureAndPrecipitation() {
		FakeClient client = new FakeClient();
		client.forecast = null;
		client.observation = KmaResponseParserTest.ncst("12.5", "1");
		WeatherSnapshot snapshot = service(client, true).lookup(LAT, LON, SAVED_AT).get();
		assertEquals("비", snapshot.getLabel());
		assertEquals("UNKNOWN", snapshot.getSkyCode());
		assertEquals(12.5, snapshot.getTemperatureC(), 0.0);
	}

	@Test
	void clearSkyIsUnknownWithoutForecastSoNoLabelMeansNoSnapshot() {
		FakeClient client = new FakeClient();
		client.forecast = null; // no precipitation + no sky -> nothing honest to show
		assertFalse(service(client, true).lookup(LAT, LON, SAVED_AT).isPresent());
	}

	@Test
	void missingOrInvalidCoordinatesYieldEmpty() {
		FakeClient client = new FakeClient();
		WeatherService service = service(client, true);
		assertFalse(service.lookup(null, null, SAVED_AT).isPresent());
		assertFalse(service.lookup(0.0, 0.0, SAVED_AT).isPresent());
		assertEquals(0, client.observationCalls);
	}

	@Test
	void sameGridAndBaseTimeHitsTheCache() {
		FakeClient client = new FakeClient();
		WeatherService service = service(client, true);
		service.lookup(LAT, LON, SAVED_AT);
		service.lookup(LAT + 0.0001, LON + 0.0001, SAVED_AT.plusMinutes(3));
		assertEquals(1, client.observationCalls);
		assertEquals(1, client.forecastCalls);
		// another hour is a different cache key
		service.lookup(LAT, LON, SAVED_AT.plusHours(2));
		assertEquals(2, client.observationCalls);
	}

	@Test
	void failuresAreCachedBrieflyToAvoidHammeringTheApi() {
		FakeClient client = new FakeClient();
		client.observation = null;
		WeatherService service = service(client, true);
		assertFalse(service.lookup(LAT, LON, SAVED_AT).isPresent());
		assertFalse(service.lookup(LAT, LON, SAVED_AT).isPresent());
		assertEquals(1, client.observationCalls);
		assertTrue(client.forecastCalls == 0);
	}
}
