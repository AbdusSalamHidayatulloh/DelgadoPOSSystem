package com.testproject.model;

public class DetailTransaksi {
    private int id;
    private int transaksiId;
    private int menuId;
    private int jumlah;
    private double subtotal;
    private String keterangan; // Kolom baru untuk menampung teks opsi/kustom

    // Konstruktor dengan 6 parameter (Ini yang menyelesaikan error Anda)
    public DetailTransaksi(int id, int transaksiId, int menuId, int jumlah, double subtotal, String keterangan) {
        this.id = id;
        this.transaksiId = transaksiId;
        this.menuId = menuId;
        this.jumlah = jumlah;
        this.subtotal = subtotal;
        this.keterangan = keterangan;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getTransaksiId() { return transaksiId; }
    public void setTransaksiId(int transaksiId) { this.transaksiId = transaksiId; }
    
    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }
    
    public int getJumlah() { return jumlah; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }
}