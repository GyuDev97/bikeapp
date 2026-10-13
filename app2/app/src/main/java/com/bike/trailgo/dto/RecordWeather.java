package com.bike.trailgo.dto;

import androidx.annotation.Nullable;

/**
 * Weather at the time a ride was saved, as normalised by the TrailGo server (the app never talks to
 * the weather provider and never interprets provider codes). Only shown when {@link #isAvailable()}.
 */
public class RecordWeather {
    private boolean available;
    private String observedAt;
    private Double temperatureC;
    private String sky;
    private String precipitation;
    private String label;

    public boolean isAvailable() {
        return available;
    }

    @Nullable
    public String getObservedAt() {
        return observedAt;
    }

    @Nullable
    public Double getTemperatureC() {
        return temperatureC;
    }

    @Nullable
    public String getSky() {
        return sky;
    }

    @Nullable
    public String getPrecipitation() {
        return precipitation;
    }

    @Nullable
    public String getLabel() {
        return label;
    }

    /** True for clear skies without precipitation; used only to pick the icon. */
    public boolean isClear() {
        return "CLEAR".equals(sky) && (precipitation == null || "NONE".equals(precipitation));
    }
}
