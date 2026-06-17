package com.testproject.service;

import com.testproject.dao.ShiftDao;
import com.testproject.db.DatabaseHelper;
import com.testproject.model.ShiftKas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class ShiftService {

    private final ShiftDao shiftDao = new ShiftDao();

    public List<ShiftKas> ambilSemuaShift() {
        return shiftDao.getAll();
    }

    public ShiftKas ambilShiftAktif() {
        return shiftDao.getShiftAktif();
    }

    public ShiftKas ambilShiftById(int id) {
        return shiftDao.getById(id);
    }

    public int bukaShiftBaru(String modalAwalStr) {
        double modal = 0;
        try {
            if (modalAwalStr != null && !modalAwalStr.trim().isEmpty()) {
                modal = Double.parseDouble(modalAwalStr.trim());
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Modal awal harus berupa angka!");
        }
        return shiftDao.bukaShift(modal);
    }

    public void tutupShift(int shiftId) {
        shiftDao.tutupShift(shiftId);
    }

    // Dipindahkan dari UI ke Service Layer agar UI tidak menyentuh SQL
    public double hitungLivePenjualan(String sejak) {
        double total = 0;
        String sql = "SELECT COALESCE(SUM(total_harga), 0) FROM transaksi WHERE status_pembayaran = 'LUNAS' AND tanggal >= ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sejak);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
            System.out.println("Error getLivePenjualan: " + e.getMessage());
        }
        return total;
    }
}