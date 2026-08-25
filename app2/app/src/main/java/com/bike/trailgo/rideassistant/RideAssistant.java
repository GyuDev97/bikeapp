package com.bike.trailgo.rideassistant;

import android.content.Context;
import android.location.Location;

import com.bike.trailgo.BuildConfig;
import com.bike.trailgo.dto.DangerousArea;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Service-owned coordinator for analysis, local safety voice, and optional remote advice. */
public final class RideAssistant {
    private final RideContextEngine contextEngine;
    private final RideStateDetector stateDetector;
    private final RideEventDetector eventDetector;
    private final DangerAreaMonitor dangerMonitor;
    private final RideVoiceManager voiceManager;
    private final GyuvisClient gyuvisClient;
    private final Map<RideEventType, Long> nextServerSpeakMillis = new EnumMap<>(RideEventType.class);
    private boolean paused;

    public RideAssistant(Context context) {
        RideAssistantPolicy policy = new RideAssistantPolicy(BuildConfig.RIDE_ASSISTANT_TEST_MODE);
        contextEngine = new RideContextEngine(policy);
        stateDetector = new RideStateDetector(policy);
        eventDetector = new RideEventDetector(policy);
        dangerMonitor = new DangerAreaMonitor(policy);
        voiceManager = new RideVoiceManager(context);
        gyuvisClient = GyuvisClient.createIfConfigured(
                BuildConfig.GYUVIS_BASE_URL,
                BuildConfig.GYUVIS_API_TOKEN
        );
    }

    public void start() {
        voiceManager.start();
    }

    public void pause(long nowMs) {
        paused = true;
        contextEngine.pause(nowMs);
    }

    public void resume(long nowMs) {
        paused = false;
        contextEngine.resume(nowMs);
    }

    public void setDangerAreas(List<DangerousArea> areas) {
        dangerMonitor.setAreas(areas);
    }

    public void onLocation(Location location) {
        if (paused || location == null) return;
        long timestamp = location.getTime() > 0L ? location.getTime() : System.currentTimeMillis();
        RideSample sample = new RideSample(timestamp, location.getLatitude(), location.getLongitude(),
                location.hasSpeed() ? location.getSpeed() * 3.6d : 0.0d,
                location.hasAltitude() ? location.getAltitude() : 0.0d,
                location.hasAccuracy() ? location.getAccuracy() : 0.0d);
        RideContext rawContext = contextEngine.add(sample);
        RideContext context = rawContext.withState(stateDetector.detect(rawContext));
        for (RideEvent event : eventDetector.detect(context)) submit(event);
        RideEvent danger = dangerMonitor.evaluate(context);
        if (danger != null) {
            voiceManager.speak(danger.localSafetyMessage, VoicePriority.HIGH);
            submit(danger);
        }
    }

    private void submit(final RideEvent event) {
        if (gyuvisClient == null) return;
        gyuvisClient.send(event, new GyuvisClient.Listener() {
            @Override
            public void onResponse(RideEvent returnedEvent, GyuvisResponse response) {
                if (!response.speak || response.message == null || response.message.trim().isEmpty()) return;
                Long nextAllowed = nextServerSpeakMillis.get(returnedEvent.type);
                if (nextAllowed != null && returnedEvent.timestampMs < nextAllowed) return;
                long cooldownMillis = Math.max(0L, response.cooldownSec) * 1000L;
                nextServerSpeakMillis.put(returnedEvent.type, returnedEvent.timestampMs + cooldownMillis);
                voiceManager.speak(response.message, VoicePriority.fromServerValue(response.priority));
            }
        });
    }

    public void shutdown() {
        voiceManager.shutdown();
    }
}
