package com.testproject.utils;

import java.util.prefs.Preferences;

public class StoreConfig {
    private static final Preferences prefs = Preferences.userNodeForPackage(StoreConfig.class);
    private static final String KEY_STORE_NAME = "STORE_NAME";

    public static String getStoreName() {
        return prefs.get(KEY_STORE_NAME, "ENATEKKU KIMBAB");
    }

    public static void setStoreName(String name) {
        prefs.put(KEY_STORE_NAME, name);
    }
}