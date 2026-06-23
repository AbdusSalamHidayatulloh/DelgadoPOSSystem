package com.testproject.service;

import com.testproject.dao.OpsiMenuDao;
import com.testproject.dao.TransaksiDao;
import com.testproject.model.DetailTransaksi;
import com.testproject.model.StatusPembayaran;
import com.testproject.model.Transaksi;

import java.util.List;

public class TransaksiService {
    private final TransaksiDao transaksiDao = new TransaksiDao();
    private final OpsiMenuDao opsiDao = new OpsiMenuDao();

    public List<Transaksi> ambilSemuaTransaksi() {
        return transaksiDao.getAll();
    }

    // METHOD BARU: Bisa menerima semua jenis status
    public void ubahStatus(int id, StatusPembayaran status) {
        transaksiDao.updateStatus(id, status.name());
    }

    public void hapusTransaksi(int id) {
        transaksiDao.delete(id);
    }

    public boolean prosesPembayaran(Transaksi t, List<DetailTransaksi> details,
                                    List<List<Integer>> checkedOpsi, List<List<Integer>> chosenPilihan) {
        int res = transaksiDao.simpanTransaksiLengkap(t, details, opsiDao, checkedOpsi, chosenPilihan);
        return res != -1;
    }
    
    public List<com.testproject.ui.transaksi.ItemKeranjang> ambilDetailKeranjang(int transaksiId) {
        return transaksiDao.getDetailKeranjang(transaksiId);
    }
}