package com.testproject.service;

import com.testproject.dao.OpsiMenuDao;
import com.testproject.dao.TransaksiDao;
import com.testproject.model.DetailTransaksi;
import com.testproject.model.Transaksi;

import java.util.List;

public class TransaksiService {
    private final TransaksiDao transaksiDao = new TransaksiDao();
    private final OpsiMenuDao opsiDao = new OpsiMenuDao();

    public List<Transaksi> ambilSemuaTransaksi() {
        return transaksiDao.getAll();
    }

    public void tandaiLunas(int id) {
        transaksiDao.updateStatus(id, "LUNAS");
    }

    public void hapusTransaksi(int id) {
        transaksiDao.delete(id);
    }

    public boolean prosesPembayaran(Transaksi t, List<DetailTransaksi> details,
                                    List<List<Integer>> checkedOpsi, List<List<Integer>> chosenPilihan) {
        // Mendelegasikan logika atomic (simpan + potong stok) ke DAO
        int res = transaksiDao.simpanTransaksiLengkap(t, details, opsiDao, checkedOpsi, chosenPilihan);
        return res != -1;
    }
}