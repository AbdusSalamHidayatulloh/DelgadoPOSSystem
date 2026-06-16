package com.testproject.model;

public enum StatusPembayaran {
    LUNAS,
    BELUM_LUNAS;

    @Override
    public String toString() {
        return switch (this) {
            case LUNAS -> "Lunas";
            case BELUM_LUNAS -> "Belum Lunas";
        };
    }
}