package com.testproject.model;

public class Transaksi {
    private int id;
    private String namaPelanggan;
    private String tanggal;
    private double totalHarga;
    private String metodeBayar;
    private StatusPembayaran statusPembayaran;

    public Transaksi() {}

    public Transaksi(int id, String namaPelanggan, String tanggal, double totalHarga, String metodeBayar, StatusPembayaran statusPembayaran) {
        this.id = id;
        this.namaPelanggan = namaPelanggan;
        this.tanggal = tanggal;
        this.totalHarga = totalHarga;
        this.metodeBayar = metodeBayar;
        this.statusPembayaran = statusPembayaran;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaPelanggan() { return namaPelanggan; }
    public void setNamaPelanggan(String namaPelanggan) { this.namaPelanggan = namaPelanggan; }

    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }

    public double getTotalHarga() { return totalHarga; }
    public void setTotalHarga(double totalHarga) { this.totalHarga = totalHarga; }

    public String getMetodeBayar() { return metodeBayar; }
    public void setMetodeBayar(String metodeBayar) { this.metodeBayar = metodeBayar; }

    public StatusPembayaran getStatusPembayaran() { return statusPembayaran; }
    public void setStatusPembayaran(StatusPembayaran statusPembayaran) { this.statusPembayaran = statusPembayaran; }
}