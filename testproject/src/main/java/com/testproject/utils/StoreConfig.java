package com.testproject.utils;

import java.util.prefs.Preferences;

public class StoreConfig {
    // Membuat penyimpan data lokal khusus untuk aplikasi ini
    private static final Preferences prefs = Preferences.userNodeForPackage(StoreConfig.class);
    private static final String KEY_STORE_NAME = "STORE_NAME";

    public static String getStoreName() {
        // Jika belum pernah disetel, defaultnya adalah "DELGADO STALL"
        return prefs.get(KEY_STORE_NAME, "DELGADO STALL");
    }

    public static void setStoreName(String name) {
        // Menyimpan nama baru ke dalam sistem
        prefs.put(KEY_STORE_NAME, name);
    }
}