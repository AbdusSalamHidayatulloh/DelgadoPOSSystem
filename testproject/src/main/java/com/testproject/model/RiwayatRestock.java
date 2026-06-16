package com.testproject.model;

public class RiwayatRestock {
    private int id;
    private int bahanId;
    private String tanggal;
    private double jumlahTambah;
    private double hargaTotal;
    private double hargaPerSatuan;

    public RiwayatRestock() {}

    public RiwayatRestock(int id, int bahanId, String tanggal, double jumlahTambah, double hargaTotal, double hargaPerSatuan) {
        this.id = id;
        this.bahanId = bahanId;
        this.tanggal = tanggal;
        this.jumlahTambah = jumlahTambah;
        this.hargaTotal = hargaTotal;
        this.hargaPerSatuan = hargaPerSatuan;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBahanId() { return bahanId; }
    public void setBahanId(int bahanId) { this.bahanId = bahanId; }

    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }

    public double getJumlahTambah() { return jumlahTambah; }
    public void setJumlahTambah(double jumlahTambah) { this.jumlahTambah = jumlahTambah; }

    public double getHargaTotal() { return hargaTotal; }
    public void setHargaTotal(double hargaTotal) { this.hargaTotal = hargaTotal; }

    public double getHargaPerSatuan() { return hargaPerSatuan; }
    public void setHargaPerSatuan(double hargaPerSatuan) { this.hargaPerSatuan = hargaPerSatuan; }
}