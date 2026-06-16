package com.testproject.model;

public class ResepItem {
    private int menuId;
    private int bahanId;
    private String namaBahan;
    private String satuan;
    private double jumlahDipakai;

    public ResepItem() {}

    public ResepItem(int menuId, int bahanId, String namaBahan, String satuan, double jumlahDipakai) {
        this.menuId = menuId;
        this.bahanId = bahanId;
        this.namaBahan = namaBahan;
        this.satuan = satuan;
        this.jumlahDipakai = jumlahDipakai;
    }

    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }

    public int getBahanId() { return bahanId; }
    public void setBahanId(int bahanId) { this.bahanId = bahanId; }

    public String getNamaBahan() { return namaBahan; }
    public void setNamaBahan(String namaBahan) { this.namaBahan = namaBahan; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public double getJumlahDipakai() { return jumlahDipakai; }
    public void setJumlahDipakai(double jumlahDipakai) { this.jumlahDipakai = jumlahDipakai; }

    @Override
    public String toString() {
        return String.format("%s — %.2f %s", namaBahan, jumlahDipakai, satuan);
    }
}