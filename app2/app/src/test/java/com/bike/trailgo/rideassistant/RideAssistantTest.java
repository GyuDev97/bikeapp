package com.bike.trailgo.rideassistant;

import com.bike.trailgo.dto.DangerousArea;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RideAssistantTest {
    @Test
    public void detectsLongClimbOnlyAfterSustainedGainAndPaceDrop() {
        RideAssistantPolicy policy = new RideAssistantPolicy();
        RideContextEngine engine = new RideContextEngine(policy);
        RideContext context = null;
        long start = 1_700_000_000_000L;
        for (int i = 0; i <= 22; i++) {
            double speed = i < 21 ? 16.0d : 8.0d;
            context = engine.add(new RideSample(start + i * 30_000L, 37.0d + i * 0.0001d,
                    127.0d, speed, 100.0d + i * 5.0d, 5.0d));
        }
        assertEquals(RideState.LONG_CLIMB, new RideStateDetector(policy).detect(context));
    }

    @Test
    public void dangerThresholdsFireOncePerDangerAndThreshold() {
        RideAssistantPolicy policy = new RideAssistantPolicy();
        DangerAreaMonitor monitor = new DangerAreaMonitor(policy);
        DangerousArea area = new DangerousArea();
        area.setDa_num(42);
        area.setDa_latitude("37.0");
        area.setDa_longitude("127.0");
        area.setDa_content("낙석 주의");
        monitor.setAreas(Collections.singletonList(area));

        RideEvent approach = monitor.evaluate(contextAt(36.9982d, 127.0d));
        assertNotNull(approach);
        assertEquals(RideEventType.DANGER_APPROACH, approach.type);
        assertNull(monitor.evaluate(contextAt(36.9982d, 127.0d)));
        assertNotNull(monitor.evaluate(contextAt(36.99955d, 127.0d)));
    }

    @Test
    public void testModeUsesFifteenSecondLongClimbWindow() {
        RideAssistantPolicy policy = new RideAssistantPolicy(true);
        assertEquals(15L, policy.longClimbMinimumSeconds);
        assertEquals(5L, policy.windowSeconds[0]);
        assertEquals(10L, policy.windowSeconds[1]);
        assertEquals(15L, policy.windowSeconds[2]);
    }

    private RideContext contextAt(double latitude, double longitude) {
        RideSample sample = new RideSample(1_700_000_000_000L, latitude, longitude,
                12.0d, 100.0d, 5.0d);
        return new RideContext(sample.timestampMs, sample, 12.0d, 12.0d, 12.0d,
                100.0d, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                100, 1000, 50, 0, RideState.FLAT);
    }
}
