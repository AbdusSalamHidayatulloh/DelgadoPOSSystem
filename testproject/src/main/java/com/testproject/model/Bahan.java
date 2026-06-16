package com.testproject.model;

public class Bahan {
    private int id;
    private String nama;
    private String satuan;
    private double jumlah;
    private double stokMinimum;

    public Bahan() {}

    public Bahan(int id, String nama, String satuan, double jumlah, double stokMinimum) {
        this.id = id;
        this.nama = nama;
        this.satuan = satuan;
        this.jumlah = jumlah;
        this.stokMinimum = stokMinimum;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public double getJumlah() { return jumlah; }
    public void setJumlah(double jumlah) { this.jumlah = jumlah; }

    public double getStokMinimum() { return stokMinimum; }
    public void setStokMinimum(double stokMinimum) { this.stokMinimum = stokMinimum; }

    public boolean isStokRendah() { return jumlah <= stokMinimum; }

    public boolean isStokHabis() { return jumlah <= 0; }

    @Override
    public String toString() { return nama; }
}