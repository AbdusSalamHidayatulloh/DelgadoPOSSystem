package com.testproject.service;

import com.testproject.dao.BahanDao;
import com.testproject.model.Bahan;
import com.testproject.model.RiwayatRestock;

import java.time.LocalDate;
import java.util.List;

public class BahanService {

    private final BahanDao bahanDao = new BahanDao();

    public List<Bahan> ambilSemuaBahan() {
        return bahanDao.getAll();
    }

    // METHOD INI SUDAH DIPERBARUI UNTUK MENERIMA 4 PARAMETER TERMASUK stokMinStr
    public boolean tambahBahanBaru(String nama, String stokStr, String stokMinStr, String satuan) {
        if (nama == null || nama.trim().isEmpty() || satuan == null || satuan.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama bahan dan satuan tidak boleh kosong!");
        }

        double stok;
        double stokMin;
        try {
            stok = Double.parseDouble(stokStr);
            stokMin = Double.parseDouble(stokMinStr);
            if (stok < 0 || stokMin < 0) {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Stok awal dan Stok Minimum harus berupa angka yang valid!");
        }

        Bahan bahanBaru = new Bahan(0, nama.trim(), satuan.trim(), stok, stokMin);
        bahanDao.add(bahanBaru);
        return true;
    }

    public boolean prosesRestock(Bahan bahan, String jumlahStr, String biayaStr, String keterangan) {
        if (bahan == null) {
            throw new IllegalArgumentException("Silakan pilih bahan terlebih dahulu!");
        }

        double jumlah;
        double biaya;
        try {
            jumlah = Double.parseDouble(jumlahStr);
            biaya = Double.parseDouble(biayaStr);
            if (jumlah <= 0 || biaya <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Jumlah dan harga harus berupa angka yang valid!");
        }

        String tanggal = LocalDate.now().toString();
        return bahanDao.restock(bahan.getId(), jumlah, biaya, tanggal);
    }

    public boolean hapusBahan(int id) {
        bahanDao.delete(id);
        return true;
    }

    public List<RiwayatRestock> ambilRiwayatRestock(int bahanId) {
        return bahanDao.getRiwayatRestock(bahanId);
    }
}