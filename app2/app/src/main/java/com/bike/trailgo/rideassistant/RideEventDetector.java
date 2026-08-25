package com.bike.trailgo.rideassistant;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Emits significant changes once, with cooldowns independent from current state. */
public final class RideEventDetector {
    private final RideAssistantPolicy policy;
    private final Map<RideEventType, Long> lastEventMillis = new EnumMap<>(RideEventType.class);
    private RideState previousState = RideState.FLAT;
    private double nextMilestoneMeters;

    public RideEventDetector(RideAssistantPolicy policy) {
        this.policy = policy;
        this.nextMilestoneMeters = policy.milestoneDistanceMeters;
    }

    public List<RideEvent> detect(RideContext context) {
        List<RideEvent> result = new ArrayList<>();
        if (context.currentRideState == RideState.LONG_CLIMB && previousState != RideState.LONG_CLIMB) {
            addIfReady(result, RideEventType.LONG_CLIMB_STARTED, context);
        }
        if (context.avgSpeed5mKmh > 0.0d
                && context.avgSpeed1mKmh + policy.paceDropKmh <= context.avgSpeed5mKmh
                && context.currentRideState != RideState.STOPPED) {
            addIfReady(result, RideEventType.PACE_DROPPED, context);
        }
        if (context.currentRideState == RideState.DESCENT
                && context.currentSpeedKmh >= policy.fastDescentKmh) {
            addIfReady(result, RideEventType.FAST_DESCENT, context);
        }
        if (context.rideDistanceMeters >= nextMilestoneMeters) {
            addIfReady(result, RideEventType.RIDE_MILESTONE, context);
            while (context.rideDistanceMeters >= nextMilestoneMeters) {
                nextMilestoneMeters += policy.milestoneDistanceMeters;
            }
        }
        previousState = context.currentRideState;
        return result;
    }

    private void addIfReady(List<RideEvent> output, RideEventType type, RideContext context) {
        Long last = lastEventMillis.get(type);
        long cooldownMillis = policy.eventCooldownSeconds * 1000L;
        if (last != null && context.timestampMs - last < cooldownMillis) return;
        lastEventMillis.put(type, context.timestampMs);
        output.add(new RideEvent(type, context.timestampMs, context));
    }
}
