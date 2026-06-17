package com.testproject.service;

import com.testproject.dao.BahanDao;
import com.testproject.dao.MenuDao;
import com.testproject.dao.OpsiMenuDao;
import com.testproject.model.*;

import java.util.List;

public class MenuService {

    private final MenuDao menuDao = new MenuDao();
    private final OpsiMenuDao opsiDao = new OpsiMenuDao();
    private final BahanDao bahanDao = new BahanDao();

    public List<MenuItem> ambilSemuaMenu() { return menuDao.getAll(); }
    public List<Bahan> ambilSemuaBahan() { return bahanDao.getAll(); }

    // ================== MENU UTAMA ==================
    public boolean simpanMenu(Integer id, String nama, String hargaStr, String tipe) {
        if (nama == null || nama.trim().isEmpty()) throw new IllegalArgumentException("Nama menu tidak boleh kosong!");
        double harga;
        try {
            harga = Double.parseDouble(hargaStr.trim());
            if (harga < 0) throw new IllegalArgumentException();
        } catch (NumberFormatException e) { throw new IllegalArgumentException("Harga harus berupa angka positif!"); }

        if (id == null) menuDao.add(new MenuItem(0, nama.trim(), harga, tipe));
        else menuDao.update(new MenuItem(id, nama.trim(), harga, tipe));
        return true;
    }

    public void hapusMenu(int menuId) {
        opsiDao.deleteByMenuId(menuId);
        menuDao.delete(menuId);
    }

    // ================== OPSI MENU ==================
    public List<OpsiMenu> ambilOpsiByMenu(int menuId) { return opsiDao.getByMenuId(menuId); }

    public boolean tambahOpsi(int menuId, String nama, String tipe, boolean wajib, String hargaStr) {
        if (nama == null || nama.trim().isEmpty()) throw new IllegalArgumentException("Nama opsi tidak boleh kosong!");
        double hargaOpsi = 0;
        if (tipe.equals("checkbox")) {
            try {
                hargaOpsi = Double.parseDouble(hargaStr.trim());
            } catch (NumberFormatException ex) { throw new IllegalArgumentException("Harga tambahan harus berupa angka!"); }
        }
        opsiDao.add(new OpsiMenu(0, menuId, nama.trim(), tipe, wajib, hargaOpsi));
        return true;
    }
    public void hapusOpsi(int opsiId) { opsiDao.delete(opsiId); }

    // ================== PILIHAN OPSI ==================
    public List<PilihanOpsi> ambilPilihanByOpsi(int opsiId) { return opsiDao.getPilihanByOpsiId(opsiId); }

    public boolean simpanPilihan(Integer id, int opsiId, String label, String hargaStr) {
        if (label == null || label.trim().isEmpty()) throw new IllegalArgumentException("Label pilihan tidak kosong!");
        double harga = 0;
        try {
            if (hargaStr != null && !hargaStr.trim().isEmpty()) harga = Double.parseDouble(hargaStr.trim());
        } catch (NumberFormatException ex) { throw new IllegalArgumentException("Harga harus angka!"); }

        if (id == null) opsiDao.addPilihan(new PilihanOpsi(0, opsiId, label.trim(), harga));
        else opsiDao.updatePilihan(new PilihanOpsi(id, opsiId, label.trim(), harga));
        return true;
    }
    public void hapusPilihan(int id) { opsiDao.deletePilihan(id); }

    // ================== RESEP ==================
    private double parseJumlah(String str) {
        try {
            double j = Double.parseDouble(str.trim());
            if (j <= 0) throw new IllegalArgumentException("Jumlah harus > 0!");
            return j;
        } catch (NumberFormatException e) { throw new IllegalArgumentException("Jumlah harus angka!"); }
    }

    public List<ResepItem> ambilResepMenu(int menuId) { return bahanDao.getResepByMenuId(menuId); }
    public List<ResepItem> ambilResepOpsi(int opsiId) { return opsiDao.getResepByOpsiId(opsiId); }
    public List<ResepItem> ambilResepPilihan(int pilihanId) { return opsiDao.getResepByPilihanId(pilihanId); }

    public void tambahResepMenu(int mId, Bahan b, String j) { if (b == null) throw new IllegalArgumentException("Pilih bahan!"); bahanDao.addResep(mId, b.getId(), parseJumlah(j)); }
    public void hapusResepMenu(int mId, int bId) { bahanDao.deleteResep(mId, bId); }

    public void tambahResepOpsi(int oId, Bahan b, String j) { if (b == null) throw new IllegalArgumentException("Pilih bahan!"); opsiDao.addResepOpsi(oId, b.getId(), parseJumlah(j)); }
    public void hapusResepOpsi(int oId, int bId) { opsiDao.deleteResepOpsi(oId, bId); }

    public void tambahResepPilihan(int pId, Bahan b, String j) { if (b == null) throw new IllegalArgumentException("Pilih bahan!"); opsiDao.addResepPilihan(pId, b.getId(), parseJumlah(j)); }
    public void hapusResepPilihan(int pId, int bId) { opsiDao.deleteResepPilihan(pId, bId); }
}