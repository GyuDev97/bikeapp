package com.bike.trailgo.ui.riding;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/** Stores a completed route outside Intent extras to avoid Binder transaction limits. */
final class RideRouteCache {
    static final String EXTRA_CACHE_FILE = "ride_route_cache_file";
    private static final String FILE_PREFIX = "ride-route-";
    private static final String FILE_SUFFIX = ".bin";

    private RideRouteCache() {
    }

    static String save(
            Context context,
            ArrayList<Double> latitudes,
            ArrayList<Double> longitudes,
            ArrayList<Double> elevations,
            ArrayList<Float> elevationSamples,
            double maxLat,
            double maxLon,
            double minLat,
            double minLon
    ) throws IOException {
        File file = File.createTempFile(FILE_PREFIX, FILE_SUFFIX, context.getCacheDir());
        RouteData data = new RouteData(
                latitudes,
                longitudes,
                elevations,
                elevationSamples,
                maxLat,
                maxLon,
                minLat,
                minLon
        );
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file))) {
            stream.writeObject(data);
        }
        return file.getName();
    }

    static RouteData load(Context context, String fileName) {
        if (fileName == null || !fileName.startsWith(FILE_PREFIX) || !fileName.endsWith(FILE_SUFFIX)) {
            return null;
        }

        File file = new File(context.getCacheDir(), fileName);
        if (!file.isFile()) {
            return null;
        }

        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file))) {
            Object data = stream.readObject();
            return data instanceof RouteData ? (RouteData) data : null;
        } catch (IOException | ClassNotFoundException ignored) {
            return null;
        }
    }

    static final class RouteData implements Serializable {
        private static final long serialVersionUID = 1L;

        final ArrayList<Double> latitudes;
        final ArrayList<Double> longitudes;
        final ArrayList<Double> elevations;
        final ArrayList<Float> elevationSamples;
        final double maxLat;
        final double maxLon;
        final double minLat;
        final double minLon;

        RouteData(
                ArrayList<Double> latitudes,
                ArrayList<Double> longitudes,
                ArrayList<Double> elevations,
                ArrayList<Float> elevationSamples,
                double maxLat,
                double maxLon,
                double minLat,
                double minLon
        ) {
            this.latitudes = new ArrayList<>(latitudes);
            this.longitudes = new ArrayList<>(longitudes);
            this.elevations = new ArrayList<>(elevations);
            this.elevationSamples = new ArrayList<>(elevationSamples);
            this.maxLat = maxLat;
            this.maxLon = maxLon;
            this.minLat = minLat;
            this.minLon = minLon;
        }
    }
}
