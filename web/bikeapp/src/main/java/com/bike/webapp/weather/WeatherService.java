package com.bike.webapp.weather;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Looks up the weather for a coordinate at a point in time.
 *
 * <ul>
 * <li>Never throws: no key, bad coordinates, timeouts and 5xx all return {@link Optional#empty()}.</li>
 * <li>Calls are cached per (grid cell, KMA base times), so riders finishing in the same area during
 * the same hour share one upstream call. Failures are cached briefly to avoid hammering the API.</li>
 * </ul>
 */
@Service
public class WeatherService {

	public static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
	private static final long SUCCESS_TTL_MS = 30L * 60L * 1000L;
	private static final long FAILURE_TTL_MS = 2L * 60L * 1000L;
	private static final int MAX_CACHE_ENTRIES = 512;

	private static final class Cached {
		final WeatherSnapshot snapshot; // null = cached failure
		final long expiresAtMillis;

		Cached(WeatherSnapshot snapshot, long expiresAtMillis) {
			this.snapshot = snapshot;
			this.expiresAtMillis = expiresAtMillis;
		}
	}

	private final KmaWeatherClient client;
	private final boolean configured;
	private final Clock clock;
	private final Map<String, Cached> cache = new LinkedHashMap<String, Cached>(64, 0.75f, true) {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Cached> eldest) {
			return size() > MAX_CACHE_ENTRIES;
		}
	};

	@Autowired
	public WeatherService(KmaHttpClient client,
			@Value("${trailgo.weather.kma-service-key:}") String serviceKey) {
		this(client, serviceKey != null && !serviceKey.trim().isEmpty(), Clock.system(KST));
	}

	WeatherService(KmaWeatherClient client, boolean configured, Clock clock) {
		this.client = client;
		this.configured = configured;
		this.clock = clock;
	}

	/**
	 * @param at the moment the ride was saved (not "now" at read time: the result is a snapshot).
	 */
	public Optional<WeatherSnapshot> lookup(Double latitude, Double longitude, LocalDateTime at) {
		if (!configured) {
			log.debug("Weather lookup skipped: KMA_SERVICE_KEY is not configured");
			return Optional.empty();
		}
		KmaGridConverter.Grid grid = KmaGridConverter.toGrid(latitude, longitude);
		if (grid == null) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(lookupCached(grid, at));
		} catch (RuntimeException e) {
			log.warn("Weather lookup failed: {} (nx={}, ny={})", e.getClass().getSimpleName(), grid.nx, grid.ny);
			return Optional.empty();
		}
	}

	/** 초단기실황 is published at HH:00 and available from about HH:10. */
	static LocalDateTime observationBase(LocalDateTime at) {
		return at.minusMinutes(10).truncatedTo(ChronoUnit.HOURS);
	}

	/** 초단기예보 is published at HH:30 and available from about HH:45. */
	static LocalDateTime forecastBase(LocalDateTime at) {
		return at.minusMinutes(45).truncatedTo(ChronoUnit.HOURS).plusMinutes(30);
	}

	private WeatherSnapshot lookupCached(KmaGridConverter.Grid grid, LocalDateTime at) {
		LocalDateTime observationBase = observationBase(at);
		LocalDateTime forecastBase = forecastBase(at);
		String key = grid.nx + ":" + grid.ny + ":" + observationBase + ":" + forecastBase;
		long now = clock.millis();

		synchronized (cache) {
			Cached hit = cache.get(key);
			if (hit != null && hit.expiresAtMillis > now) {
				return hit.snapshot;
			}
		}

		WeatherSnapshot fresh = fetch(grid, at, observationBase, forecastBase);
		synchronized (cache) {
			cache.put(key, new Cached(fresh, now + (fresh == null ? FAILURE_TTL_MS : SUCCESS_TTL_MS)));
		}
		return fresh;
	}

	private WeatherSnapshot fetch(KmaGridConverter.Grid grid, LocalDateTime at,
			LocalDateTime observationBase, LocalDateTime forecastBase) {
		KmaResponseParser.Observation observation = KmaResponseParser
				.parseObservation(client.fetchObservation(grid.nx, grid.ny, observationBase));
		if (observation == null) {
			return null;
		}
		// Sky is only in the forecast product; a failure here still leaves rain/snow and temperature.
		String sky = KmaResponseParser.parseSky(client.fetchForecast(grid.nx, grid.ny, forecastBase), at);
		String label = KmaResponseParser.labelFor(sky, observation.precipitationCode);
		if (label == null) {
			return null;
		}
		return new WeatherSnapshot(observation.observedAt, grid.nx, grid.ny, observation.temperatureC,
				sky == null ? WeatherSnapshot.SKY_UNKNOWN : sky, observation.precipitationCode, label);
	}
}
