package com.example.waviapp.utils;

import android.content.Context;
import java.io.File;

/**
 * Centralized configuration for WaviApp.
 * Contains Firebase Storage base URL and offline asset path constants.
 */
public final class AppConfig {

    private AppConfig() {} // Prevent instantiation

    // ========== GitHub jsDelivr CDN ==========
    public static final String FIREBASE_STORAGE_BASE_URL =
            "https://cdn.jsdelivr.net/gh/MinhTuan1412/wavi-assets@main/";

    // ========== Offline Asset Paths ==========
    public static final String OFFLINE_DIR_NAME = "offline_assets";

    // ========== Audio Folder Mapping ==========
    public static final String AUDIO_P1_FOLDER = "audio_p1";
    public static final String AUDIO_P2_FOLDER = "audio_p2";
    public static final String AUDIO_P3_FOLDER = "audio_p3";
    public static final String AUDIO_P4_FOLDER = "audio_p4";
    public static final String IMG_P1_FOLDER = "img_p1";

    // ========== UI Design Constants ==========
    public static final int CORNER_RADIUS_DP = 12;

    /**
     * Returns the root directory for offline downloaded assets.
     * Creates the directory if it doesn't exist.
     */
    public static File getOfflineDir(Context context) {
        File dir = new File(context.getFilesDir(), OFFLINE_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Returns the local file path for a specific asset.
     * e.g., relativePath = "audio_p1/q1.mp3"
     */
    public static File getLocalAssetFile(Context context, String relativePath) {
        return new File(getOfflineDir(context), relativePath);
    }

    /**
     * Returns the CDN download URL for a given relative path.
     */
    public static String getFirebaseUrl(String relativePath) {
        return FIREBASE_STORAGE_BASE_URL + relativePath;
    }
}
