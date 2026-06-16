package com.testproject.model;

public class OpsiMenu {
    private int id;
    private int menuId;
    private String namaOpsi;
    private String tipeOpsi; // "checkbox" or "pilihan"
    private boolean wajib;
    private double hargaTambahan;

    public OpsiMenu() {}

    public OpsiMenu(int id, int menuId, String namaOpsi, String tipeOpsi, boolean wajib, double hargaTambahan) {
        this.id = id;
        this.menuId = menuId;
        this.namaOpsi = namaOpsi;
        this.tipeOpsi = tipeOpsi;
        this.wajib = wajib;
        this.hargaTambahan = hargaTambahan;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }

    public String getNamaOpsi() { return namaOpsi; }
    public void setNamaOpsi(String namaOpsi) { this.namaOpsi = namaOpsi; }

    public String getTipeOpsi() { return tipeOpsi; }
    public void setTipeOpsi(String tipeOpsi) { this.tipeOpsi = tipeOpsi; }

    public boolean isWajib() { return wajib; }
    public void setWajib(boolean wajib) { this.wajib = wajib; }

    public double getHargaTambahan() { return hargaTambahan; }
    public void setHargaTambahan(double hargaTambahan) { this.hargaTambahan = hargaTambahan; }

    @Override
    public String toString() { return namaOpsi; }
}