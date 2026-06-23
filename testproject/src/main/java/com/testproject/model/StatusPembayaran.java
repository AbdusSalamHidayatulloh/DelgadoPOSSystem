package com.testproject.model;

public enum StatusPembayaran {
    LUNAS,
    REFUND,
    BATAL,
    SEDANG_DIPROSES;

    @Override
    public String toString() {
        return switch (this) {
            case LUNAS -> "Lunas";
            case REFUND -> "Refund";
            case BATAL -> "Batal";
            case SEDANG_DIPROSES -> "Sedang Diproses";
        };
    }
}