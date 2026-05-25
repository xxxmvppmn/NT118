package com.example.waviapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages offline assets: downloading, checking, and resolving paths.
 * Smart Path Resolver: If file exists locally → use local. Otherwise → use online URL.
 */
public class OfflineAssetManager {

    private static final String TAG = "OfflineAssetManager";
    private static final String PREFS_NAME = "offline_prefs";
    private static final String KEY_PREFIX_DOWNLOADED = "downloaded_";

    private final Context context;
    private final SharedPreferences prefs;
    private final ExecutorService executor;

    public OfflineAssetManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newFixedThreadPool(3);
    }

    // ==================== Path Resolution ====================

    /**
     * Smart path resolver.
     * Returns local file path if downloaded, otherwise returns Firebase URL.
     * 
     * @param relativePath e.g., "audio_p1/q1.mp3"
     * @return absolute local path or Firebase URL string
     */
    public String resolveAssetPath(String relativePath) {
        File localFile = AppConfig.getLocalAssetFile(context, relativePath);
        if (localFile.exists()) {
            return localFile.getAbsolutePath();
        }
        return AppConfig.getFirebaseUrl(relativePath);
    }

    /**
     * Check if a specific asset file has been downloaded locally.
     */
    public boolean isAssetAvailable(String relativePath) {
        return AppConfig.getLocalAssetFile(context, relativePath).exists();
    }

    /**
     * Check if an entire set has been downloaded.
     * 
     * @param setKey unique identifier for the set, e.g., "part1_set0"
     */
    public boolean isSetDownloaded(String setKey) {
        return prefs.getBoolean(KEY_PREFIX_DOWNLOADED + setKey, false);
    }

    // ==================== Network Check ====================

    /**
     * Check if the device is connected to the internet.
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    /**
     * Check if an asset can be used (either locally available or network is available).
     */
    public boolean canAccessAsset(String relativePath) {
        return isAssetAvailable(relativePath) || isNetworkAvailable();
    }

    // ==================== Download Management ====================

    /**
     * Downloads a list of files for a specific set.
     * Runs entirely on background threads.
     *
     * @param setKey    unique identifier for the set
     * @param filePaths list of relative file paths to download
     * @param callback  progress and completion callback
     */
    public void downloadSet(String setKey, List<String> filePaths, DownloadCallback callback) {
        if (filePaths == null || filePaths.isEmpty()) {
            if (callback != null) callback.onComplete(true);
            return;
        }

        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        int total = filePaths.size();

        for (String relativePath : filePaths) {
            executor.execute(() -> {
                boolean success = downloadSingleFile(relativePath);
                if (!success) {
                    failed.incrementAndGet();
                }

                int done = completed.incrementAndGet();
                int progress = (int) ((done / (float) total) * 100);

                if (callback != null) {
                    callback.onProgress(progress, done, total);
                }

                if (done == total) {
                    boolean allSuccess = failed.get() == 0;
                    if (allSuccess) {
                        prefs.edit().putBoolean(KEY_PREFIX_DOWNLOADED + setKey, true).apply();
                    }
                    if (callback != null) {
                        callback.onComplete(allSuccess);
                    }
                }
            });
        }
    }

    /**
     * Downloads a single file from Firebase Storage to local storage.
     */
    private boolean downloadSingleFile(String relativePath) {
        File localFile = AppConfig.getLocalAssetFile(context, relativePath);

        // Skip if already downloaded
        if (localFile.exists()) {
            return true;
        }

        // Create parent directories
        File parentDir = localFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(relativePath);
            // Use Tasks.await for synchronous download on background thread
            com.google.android.gms.tasks.Tasks.await(storageRef.getFile(localFile));
            Log.d(TAG, "Downloaded: " + relativePath);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to download: " + relativePath + " - " + e.getMessage());
            // Cleanup partial file
            if (localFile.exists()) {
                localFile.delete();
            }
            return false;
        }
    }

    /**
     * Delete all downloaded files for a specific set.
     */
    public void deleteSet(String setKey, List<String> filePaths) {
        executor.execute(() -> {
            if (filePaths != null) {
                for (String relativePath : filePaths) {
                    File file = AppConfig.getLocalAssetFile(context, relativePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
            prefs.edit().remove(KEY_PREFIX_DOWNLOADED + setKey).apply();
            Log.d(TAG, "Deleted set: " + setKey);
        });
    }

    /**
     * Get the total size of offline downloaded assets in bytes.
     */
    public long getOfflineStorageUsed() {
        return getDirSize(AppConfig.getOfflineDir(context));
    }

    private long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getDirSize(file);
                    }
                }
            }
        }
        return size;
    }

    // ==================== Callback Interface ====================

    public interface DownloadCallback {
        void onProgress(int percent, int completed, int total);
        void onComplete(boolean success);
    }
}
