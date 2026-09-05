package com.bike.trailgo.rideassistant;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.bike.trailgo.BuildConfig;
import com.bike.trailgo.dto.DangerousArea;
import com.bike.trailgo.setting.AccountSession;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Service-owned coordinator for analysis, local safety voice, and optional remote advice. */
public final class RideAssistant {
    private static final String TAG = "RideAssistant";
    private final RideContextEngine contextEngine;
    private final RideStateDetector stateDetector;
    private final RideEventDetector eventDetector;
    private final DangerAreaMonitor dangerMonitor;
    private final RideVoiceManager voiceManager;
    private final GyuvisClient gyuvisClient;
    private final String rideId = UUID.randomUUID().toString();
    private final boolean gyuvisEnabled;
    private final Observer observer;
    private final Map<RideEventType, Long> nextServerSpeakMillis = new EnumMap<>(RideEventType.class);
    private boolean paused;
    private boolean voiceGuidanceEnabled = true;
    private boolean dangerAlertsEnabled = true;
    private RideState lastLoggedState = RideState.FLAT;

    public interface Observer {
        void onContext(RideContext context);
        void onEvent(RideEvent event, String reason);
        void onGyuvisStatus(RideEvent event, String status, boolean speak, String priority, String message);
    }

    public RideAssistant(Context context) {
        this(context, true, null);
    }

    public RideAssistant(Context context, boolean gyuvisEnabled, Observer observer) {
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
        // Guest rides remain fully usable without an account or a network connection.
        // Local safety alerts continue; only optional remote coaching is skipped.
        this.gyuvisEnabled = gyuvisEnabled && !AccountSession.isGuest(context);
        this.observer = observer;
    }

    public void start() {
        if (voiceGuidanceEnabled) voiceManager.start();
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

    public void setVoiceGuidanceEnabled(boolean enabled) {
        voiceGuidanceEnabled = enabled;
        if (enabled) {
            voiceManager.start();
        } else {
            voiceManager.stop();
        }
    }

    public void setDangerAlertsEnabled(boolean enabled) {
        dangerAlertsEnabled = enabled;
    }

    /** A local course-following warning from the foreground tracking flow. */
    public void announceCourseGuidance(String message) {
        speakIfEnabled(message, VoicePriority.HIGH);
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
        logContext(context);
        if (observer != null) observer.onContext(context);
        for (RideEvent event : eventDetector.detect(context)) submit(event);
        RideEvent danger = dangerAlertsEnabled ? dangerMonitor.evaluate(context) : null;
        if (danger != null) {
            speakIfEnabled(danger.localSafetyMessage, VoicePriority.HIGH);
            submit(danger);
        }
    }

    private void submit(final RideEvent event) {
        String reason = eventReason(event);
        if (observer != null) observer.onEvent(event, reason);
        if (BuildConfig.DEBUG) Log.d(TAG, "[EVENT] ride_id=" + rideId + " event_id="
                + event.eventId + " event_type=" + event.type + " reason=" + reason);
        if (!gyuvisEnabled) {
            if (observer != null) observer.onGyuvisStatus(event, "OFF", false, null, null);
            return;
        }
        if (gyuvisClient == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "GYUVIS request skipped: client is not configured");
            if (observer != null) observer.onGyuvisStatus(event, "UNCONFIGURED", false, null, null);
            return;
        }
        gyuvisClient.send(rideId, event, new GyuvisClient.Listener() {
            @Override
            public void onResponse(RideEvent returnedEvent, GyuvisResponse response, int httpStatus) {
                if (BuildConfig.DEBUG) Log.d(TAG, "GYUVIS response event_id=" + returnedEvent.eventId
                        + " speak=" + response.speak + " priority=" + response.priority);
                if (observer != null) observer.onGyuvisStatus(returnedEvent, "HTTP " + httpStatus,
                        response.speak, response.priority, response.message);
                if (!response.speak || response.message == null || response.message.trim().isEmpty()) return;
                Long nextAllowed = nextServerSpeakMillis.get(returnedEvent.type);
                if (nextAllowed != null && returnedEvent.timestampMs < nextAllowed) return;
                long cooldownMillis = Math.max(0L, response.cooldownSec) * 1000L;
                nextServerSpeakMillis.put(returnedEvent.type, returnedEvent.timestampMs + cooldownMillis);
                speakIfEnabled(response.message, VoicePriority.fromServerValue(response.priority));
            }

            @Override
            public void onFailure(RideEvent returnedEvent, String reason) {
                if (observer != null) observer.onGyuvisStatus(returnedEvent, reason, false, null, null);
            }
        });
    }

    private void logContext(RideContext context) {
        if (!BuildConfig.DEBUG) return;
        Log.d(TAG, "[CONTEXT] ride_id=" + rideId + " state=" + context.currentRideState
                + " speed=" + context.currentSpeedKmh + " avg_1m=" + context.avgSpeed1mKmh
                + " avg_5m=" + context.avgSpeed5mKmh + " avg_10m=" + context.avgSpeed10mKmh
                + " altitude=" + context.currentAltitudeMeters + " grade=" + context.estimatedGradePercent
                + " climb_sec=" + context.continuousClimbSeconds + " descent_sec="
                + context.continuousDescentSeconds + " elevation_gain=" + context.elevationGain10mMeters);
        Log.d(TAG, "[STATE] previous_state=" + lastLoggedState + " current_state=" + context.currentRideState);
        lastLoggedState = context.currentRideState;
    }

    private void speakIfEnabled(String message, VoicePriority priority) {
        if (voiceGuidanceEnabled) {
            voiceManager.speak(message, priority);
        }
    }

    private String eventReason(RideEvent event) {
        switch (event.type) {
            case LONG_CLIMB_STARTED:
                return "long climb duration and elevation gain thresholds met";
            case PACE_DROPPED:
                return "sustained pace drop while climbing";
            case FAST_DESCENT:
                return "descent state and speed threshold met";
            case DANGER_APPROACH:
                return "nearest danger distance=" + Math.round(event.dangerDistanceMeters);
            default:
                return "event threshold met";
        }
    }

    public void shutdown() {
        voiceManager.shutdown();
    }
}
