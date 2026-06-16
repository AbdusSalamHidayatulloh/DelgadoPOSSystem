package com.testproject.model;

public class ShiftKas {
    private int id;
    private String tanggalBuka;
    private String tanggalTutup; // null if still open
    private double modalAwal;
    private double totalPenjualan;
    private double totalKas; // modalAwal + totalPenjualan

    public ShiftKas() {}

    public ShiftKas(int id, String tanggalBuka, String tanggalTutup,
                    double modalAwal, double totalPenjualan, double totalKas) {
        this.id = id;
        this.tanggalBuka = tanggalBuka;
        this.tanggalTutup = tanggalTutup;
        this.modalAwal = modalAwal;
        this.totalPenjualan = totalPenjualan;
        this.totalKas = totalKas;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTanggalBuka() { return tanggalBuka; }
    public void setTanggalBuka(String tanggalBuka) { this.tanggalBuka = tanggalBuka; }

    public String getTanggalTutup() { return tanggalTutup; }
    public void setTanggalTutup(String tanggalTutup) { this.tanggalTutup = tanggalTutup; }

    public double getModalAwal() { return modalAwal; }
    public void setModalAwal(double modalAwal) { this.modalAwal = modalAwal; }

    public double getTotalPenjualan() { return totalPenjualan; }
    public void setTotalPenjualan(double totalPenjualan) { this.totalPenjualan = totalPenjualan; }

    public double getTotalKas() { return totalKas; }
    public void setTotalKas(double totalKas) { this.totalKas = totalKas; }

    public boolean isOpen() { return tanggalTutup == null || tanggalTutup.isEmpty(); }
}