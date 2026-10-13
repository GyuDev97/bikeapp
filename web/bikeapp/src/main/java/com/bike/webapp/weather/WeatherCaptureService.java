package com.bike.webapp.weather;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bike.webapp.service.AndroidService;

/**
 * Attaches a weather snapshot to a ride that was just saved.
 *
 * <p>It runs after the record is committed and off the request thread, so a missing key, a slow or
 * failing KMA API, or a full queue can only ever leave the weather columns NULL; saving and reading
 * rides is never affected. The snapshot is written once ("only if still NULL") and is never
 * refreshed afterwards.</p>
 */
@Service
public class WeatherCaptureService {

	private static final Logger log = LoggerFactory.getLogger(WeatherCaptureService.class);

	private final WeatherService weatherService;
	private final AndroidService androidService;
	private final Executor executor;
	private final ExecutorService ownedExecutor;

	@Autowired
	public WeatherCaptureService(WeatherService weatherService, AndroidService androidService) {
		this.weatherService = weatherService;
		this.androidService = androidService;
		RejectedExecutionHandler dropAndLog = (task, pool) -> log.warn("Weather capture queue is full; skipping snapshot");
		ThreadFactory daemon = runnable -> {
			Thread thread = new Thread(runnable, "weather-capture");
			thread.setDaemon(true);
			return thread;
		};
		this.ownedExecutor = new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(100), daemon, dropAndLog);
		this.executor = ownedExecutor;
	}

	/** For tests: lets the capture run synchronously. */
	WeatherCaptureService(WeatherService weatherService, AndroidService androidService, Executor executor) {
		this.weatherService = weatherService;
		this.androidService = androidService;
		this.executor = executor;
		this.ownedExecutor = null;
	}

	/**
	 * Schedules the snapshot. Never throws and never blocks on the network.
	 *
	 * @param savedAtKst when the ride was saved (Asia/Seoul wall clock)
	 */
	public void captureAsync(final int rrNum, final Double latitude, final Double longitude,
			final LocalDateTime savedAtKst) {
		if (latitude == null || longitude == null) {
			return;
		}
		try {
			executor.execute(() -> capture(rrNum, latitude, longitude, savedAtKst));
		} catch (RuntimeException e) {
			log.warn("Weather capture could not be scheduled: {}", e.getClass().getSimpleName());
		}
	}

	void capture(int rrNum, Double latitude, Double longitude, LocalDateTime savedAtKst) {
		try {
			Optional<WeatherSnapshot> snapshot = weatherService.lookup(latitude, longitude, savedAtKst);
			if (!snapshot.isPresent()) {
				return;
			}
			int updated = androidService.saveWeatherSnapshot(rrNum, snapshot.get());
			log.debug("Weather snapshot stored for record {} (rows={})", rrNum, updated);
		} catch (RuntimeException e) {
			log.warn("Weather snapshot for record {} was not stored: {}", rrNum, e.getClass().getSimpleName());
		}
	}

	@PreDestroy
	void shutdown() {
		if (ownedExecutor != null) {
			ownedExecutor.shutdown();
		}
	}
}
