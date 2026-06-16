package com.testproject.model;

public class MenuItem {
    private int id;
    private String nama;
    private double harga;
    private String tipe;

    public MenuItem() {}

    public MenuItem(int id, String nama, double harga, String tipe) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
        this.tipe = tipe;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public double getHarga() { return harga; }
    public void setHarga(double harga) { this.harga = harga; }

    public String getTipe() { return tipe; }
    public void setTipe(String tipe) { this.tipe = tipe; }

    @Override
    public String toString() { return nama; }
}