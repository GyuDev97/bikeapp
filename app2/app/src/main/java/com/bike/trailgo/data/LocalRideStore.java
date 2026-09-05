package com.bike.trailgo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AtomicFile;

import androidx.annotation.Nullable;

import com.bike.trailgo.dto.RidingRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Private, device-local records made while the rider is using guest mode. */
public final class LocalRideStore {
    private static final String DIRECTORY = "guest-rides";
    private static final String INDEX_FILE = "records.json";
    private static final String GPX_PREFIX = "local:";
    private static final String PREFS = "guest_ride_store";
    private static final String KEY_NEXT_ID = "next_id";
    private static final Gson GSON = new Gson();
    private static final Type RECORD_LIST_TYPE = new TypeToken<ArrayList<RidingRecord>>() { }.getType();

    private LocalRideStore() {
    }

    public static synchronized RidingRecord save(
            Context context,
            String name,
            int distanceMeters,
            int topSpeedKmh,
            int averageSpeedKmh,
            int elevationGainMeters,
            int breakSeconds,
            int durationSeconds,
            String area,
            File sourceGpx
    ) throws IOException {
        if (sourceGpx == null || !sourceGpx.isFile() || sourceGpx.length() == 0L) {
            throw new IOException("Missing guest GPX file");
        }

        Context appContext = context.getApplicationContext();
        File directory = getDirectory(appContext);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create guest ride directory");
        }

        ArrayList<RidingRecord> records = new ArrayList<>(load(appContext));
        String sourceId = sourceGpx.getName();
        for (RidingRecord existing : records) {
            if (sourceId.equals(existing.getLocal_source_id())) {
                // The previous attempt completed before its UI/process could clear the
                // unfinished-ride checkpoint. Do not create a duplicate on retry.
                return existing;
            }
        }

        int localId = nextLocalId(appContext);
        String gpxName = "ride-" + Math.abs((long) localId) + ".gpx";
        File targetGpx = new File(directory, gpxName);
        copyAtomically(sourceGpx, targetGpx);

        RidingRecord record = new RidingRecord();
        record.setRr_num(localId);
        record.setRr_rider("guest");
        record.setRr_date(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(new Date()));
        record.setRr_name(isBlank(name) ? "라이딩 기록" : name.trim());
        record.setRr_distance(Math.max(0, distanceMeters));
        record.setRr_topspeed(Math.max(0, topSpeedKmh));
        record.setRr_avgspeed(Math.max(0, averageSpeedKmh));
        record.setRr_high(Math.max(0, elevationGainMeters));
        record.setRr_breaktime(Math.max(0, breakSeconds));
        record.setRr_time(Math.max(0, durationSeconds));
        record.setRr_area(area == null ? "" : area);
        // Guest records are always private and their GPX name is never sent to the server.
        record.setRr_open(0);
        record.setRr_gpx(GPX_PREFIX + gpxName);
        record.setLocal_source_id(sourceId);

        records.add(record);
        sortNewestFirst(records);
        try {
            writeIndex(appContext, records);
        } catch (IOException error) {
            // A record without an index must not become a visible partial save.
            new AtomicFile(targetGpx).delete();
            throw error;
        }
        return record;
    }

    public static synchronized ArrayList<RidingRecord> load(Context context) {
        AtomicFile index = new AtomicFile(new File(getDirectory(context.getApplicationContext()), INDEX_FILE));
        if (!index.getBaseFile().isFile()) {
            return new ArrayList<>();
        }
        try (InputStream input = index.openRead()) {
            byte[] bytes = new byte[(int) index.getBaseFile().length()];
            int offset = 0;
            while (offset < bytes.length) {
                int read = input.read(bytes, offset, bytes.length - offset);
                if (read < 0) break;
                offset += read;
            }
            ArrayList<RidingRecord> records = GSON.fromJson(new String(bytes, 0, offset, "UTF-8"), RECORD_LIST_TYPE);
            if (records == null) return new ArrayList<>();
            sortNewestFirst(records);
            return records;
        } catch (Exception ignored) {
            quarantineUnreadableIndex(index.getBaseFile());
            return new ArrayList<>();
        }
    }

    public static boolean hasRecords(Context context) {
        return !load(context).isEmpty();
    }

    public static boolean isLocalGpxReference(@Nullable String reference) {
        return reference != null && reference.startsWith(GPX_PREFIX);
    }

    public static InputStream openGpx(Context context, String reference) throws IOException {
        if (!isLocalGpxReference(reference)) {
            throw new IOException("Not a local GPX reference");
        }
        String fileName = reference.substring(GPX_PREFIX.length());
        if (!fileName.matches("ride-[0-9]+\\.gpx")) {
            throw new IOException("Invalid local GPX name");
        }
        File file = new File(getDirectory(context.getApplicationContext()), fileName);
        if (!file.isFile()) {
            throw new IOException("Local GPX file not found");
        }
        return new FileInputStream(file);
    }

    /** Removes a guest record and its private GPX file. Server records are never touched. */
    public static synchronized boolean delete(Context context, int localId) throws IOException {
        if (localId >= 0) {
            return false;
        }
        Context appContext = context.getApplicationContext();
        ArrayList<RidingRecord> records = new ArrayList<>(load(appContext));
        RidingRecord target = null;
        for (RidingRecord record : records) {
            if (record.getRr_num() == localId) {
                target = record;
                break;
            }
        }
        if (target == null) {
            return false;
        }
        records.remove(target);
        writeIndex(appContext, records);
        String gpxReference = target.getRr_gpx();
        if (isLocalGpxReference(gpxReference)) {
            String fileName = gpxReference.substring(GPX_PREFIX.length());
            if (fileName.matches("ride-[0-9]+\\.gpx")) {
                new File(getDirectory(appContext), fileName).delete();
            }
        }
        return true;
    }

    private static void writeIndex(Context context, List<RidingRecord> records) throws IOException {
        File directory = getDirectory(context);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create guest ride directory");
        }
        AtomicFile index = new AtomicFile(new File(directory, INDEX_FILE));
        FileOutputStream output = null;
        try {
            output = index.startWrite();
            Writer writer = new OutputStreamWriter(output, "UTF-8");
            writer.write(GSON.toJson(records));
            writer.flush();
            index.finishWrite(output);
        } catch (IOException exception) {
            if (output != null) index.failWrite(output);
            throw exception;
        }
    }

    private static int nextLocalId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int id = preferences.getInt(KEY_NEXT_ID, -1);
        if (id >= 0 || id == Integer.MIN_VALUE) id = -1;
        preferences.edit().putInt(KEY_NEXT_ID, id - 1).apply();
        return id;
    }

    private static File getDirectory(Context context) {
        return new File(context.getFilesDir(), DIRECTORY);
    }

    private static void copyAtomically(File source, File target) throws IOException {
        AtomicFile targetFile = new AtomicFile(target);
        FileOutputStream output = null;
        try (FileInputStream input = new FileInputStream(source)) {
            output = targetFile.startWrite();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            output.getFD().sync();
            targetFile.finishWrite(output);
        } catch (IOException error) {
            if (output != null) targetFile.failWrite(output);
            throw error;
        }
    }

    private static void quarantineUnreadableIndex(File index) {
        if (!index.isFile()) return;
        File parent = index.getParentFile();
        if (parent == null) return;
        File backup = new File(parent, "records-corrupt-" + System.currentTimeMillis() + ".json");
        // Best effort only: read failures must never crash or block a new local ride.
        index.renameTo(backup);
    }

    private static void sortNewestFirst(ArrayList<RidingRecord> records) {
        Collections.sort(records, new Comparator<RidingRecord>() {
            @Override
            public int compare(RidingRecord left, RidingRecord right) {
                String leftDate = left.getRr_date() == null ? "" : left.getRr_date();
                String rightDate = right.getRr_date() == null ? "" : right.getRr_date();
                return rightDate.compareTo(leftDate);
            }
        });
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
