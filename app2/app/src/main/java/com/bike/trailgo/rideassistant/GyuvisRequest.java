package com.bike.trailgo.rideassistant;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

final class GyuvisRequest {
    @SerializedName("event_type") final String eventType;
    final String timestamp;
    @SerializedName("ride_context") final RideContextPayload rideContext;

    private GyuvisRequest(String eventType, String timestamp, RideContextPayload rideContext) {
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.rideContext = rideContext;
    }

    static GyuvisRequest from(RideEvent event) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new GyuvisRequest(event.type.name(), format.format(new Date(event.timestampMs)),
                new RideContextPayload(event.context));
    }

    static final class RideContextPayload {
        @SerializedName("current_speed") final double currentSpeed;
        @SerializedName("current_altitude") final double currentAltitude;
        @SerializedName("avg_speed_1m") final double avgSpeed1m;
        @SerializedName("avg_speed_5m") final double avgSpeed5m;
        @SerializedName("avg_speed_10m") final double avgSpeed10m;
        @SerializedName("climb_duration_sec") final long climbDurationSec;
        @SerializedName("descent_duration_sec") final long descentDurationSec;
        @SerializedName("elevation_gain") final double elevationGain;
        @SerializedName("ride_distance") final double rideDistance;
        @SerializedName("ride_duration_sec") final long rideDurationSec;
        @SerializedName("total_elevation_gain") final double totalElevationGain;
        @SerializedName("ride_state") final String rideState;

        RideContextPayload(RideContext context) {
            currentSpeed = context.currentSpeedKmh;
            currentAltitude = context.currentAltitudeMeters;
            avgSpeed1m = context.avgSpeed1mKmh;
            avgSpeed5m = context.avgSpeed5mKmh;
            avgSpeed10m = context.avgSpeed10mKmh;
            climbDurationSec = context.continuousClimbSeconds;
            descentDurationSec = context.continuousDescentSeconds;
            elevationGain = context.elevationGain10mMeters;
            rideDistance = context.rideDistanceMeters;
            rideDurationSec = context.rideDurationSeconds;
            totalElevationGain = context.totalElevationGainMeters;
            rideState = context.currentRideState.name();
        }
    }
}
