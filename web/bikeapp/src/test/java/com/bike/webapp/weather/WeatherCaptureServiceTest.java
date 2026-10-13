package com.bike.webapp.weather;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.bike.webapp.service.AndroidService;

class WeatherCaptureServiceTest {

	private static final LocalDateTime SAVED_AT = LocalDateTime.of(2026, 10, 10, 19, 49);
	private final Executor direct = Runnable::run;

	private WeatherService weather(WeatherServiceTest.FakeClient client, boolean configured) {
		return new WeatherService(client, configured, Clock.fixed(Instant.parse("2026-10-10T10:49:00Z"), WeatherService.KST));
	}

	@Test
	void storesTheSnapshotForTheSavedRecord() {
		AndroidService androidService = mock(AndroidService.class);
		WeatherCaptureService capture = new WeatherCaptureService(
				weather(new WeatherServiceTest.FakeClient(), true), androidService, direct);

		capture.captureAsync(123, 35.8329, 128.5650, SAVED_AT);

		ArgumentCaptor<WeatherSnapshot> stored = ArgumentCaptor.forClass(WeatherSnapshot.class);
		verify(androidService).saveWeatherSnapshot(org.mockito.ArgumentMatchers.eq(123), stored.capture());
		org.junit.jupiter.api.Assertions.assertEquals("맑음", stored.getValue().getLabel());
		org.junit.jupiter.api.Assertions.assertEquals(21.0, stored.getValue().getTemperatureC(), 0.0);
	}

	@Test
	void noKeyLeavesWeatherNullAndStoresNothing() {
		AndroidService androidService = mock(AndroidService.class);
		WeatherCaptureService capture = new WeatherCaptureService(
				weather(new WeatherServiceTest.FakeClient(), false), androidService, direct);

		assertDoesNotThrow(() -> capture.captureAsync(123, 35.8329, 128.5650, SAVED_AT));
		verify(androidService, never()).saveWeatherSnapshot(anyInt(), any(WeatherSnapshot.class));
	}

	@Test
	void missingCoordinatesStoreNothing() {
		AndroidService androidService = mock(AndroidService.class);
		WeatherCaptureService capture = new WeatherCaptureService(
				weather(new WeatherServiceTest.FakeClient(), true), androidService, direct);

		capture.captureAsync(123, null, null, SAVED_AT);
		capture.captureAsync(123, 35.8, null, SAVED_AT);
		verify(androidService, never()).saveWeatherSnapshot(anyInt(), any(WeatherSnapshot.class));
	}

	@Test
	void externalApiTimeoutNeverPropagates() {
		AndroidService androidService = mock(AndroidService.class);
		WeatherServiceTest.FakeClient client = new WeatherServiceTest.FakeClient();
		client.throwOnCall = true;
		WeatherCaptureService capture = new WeatherCaptureService(weather(client, true), androidService, direct);

		assertDoesNotThrow(() -> capture.captureAsync(123, 35.8329, 128.5650, SAVED_AT));
		verify(androidService, never()).saveWeatherSnapshot(anyInt(), any(WeatherSnapshot.class));
	}

	@Test
	void databaseFailureWhileStoringNeverPropagates() {
		AndroidService androidService = mock(AndroidService.class);
		when(androidService.saveWeatherSnapshot(anyInt(), any(WeatherSnapshot.class)))
				.thenThrow(new IllegalStateException("db down"));
		WeatherCaptureService capture = new WeatherCaptureService(
				weather(new WeatherServiceTest.FakeClient(), true), androidService, direct);

		assertDoesNotThrow(() -> capture.captureAsync(123, 35.8329, 128.5650, SAVED_AT));
	}

	@Test
	void rejectedExecutorNeverPropagates() {
		AndroidService androidService = mock(AndroidService.class);
		Executor rejecting = task -> {
			throw new java.util.concurrent.RejectedExecutionException("full");
		};
		WeatherCaptureService capture = new WeatherCaptureService(
				weather(new WeatherServiceTest.FakeClient(), true), androidService, rejecting);

		assertDoesNotThrow(() -> capture.captureAsync(123, 35.8329, 128.5650, SAVED_AT));
	}
}
