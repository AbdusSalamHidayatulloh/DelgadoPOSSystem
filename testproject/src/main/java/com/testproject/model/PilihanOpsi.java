package com.testproject.model;

public class PilihanOpsi {
    private int id;
    private int opsiId;
    private String label;
    private double hargaTambahan;

    public PilihanOpsi() {}

    public PilihanOpsi(int id, int opsiId, String label, double hargaTambahan) {
        this.id = id;
        this.opsiId = opsiId;
        this.label = label;
        this.hargaTambahan = hargaTambahan;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOpsiId() { return opsiId; }
    public void setOpsiId(int opsiId) { this.opsiId = opsiId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public double getHargaTambahan() { return hargaTambahan; }
    public void setHargaTambahan(double hargaTambahan) { this.hargaTambahan = hargaTambahan; }

    @Override
    public String toString() { return label; }
}