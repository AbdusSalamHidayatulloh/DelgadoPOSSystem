package com.testproject.service;

import com.testproject.db.DatabaseHelper;
import com.testproject.model.RiwayatRestock;
import com.testproject.model.StatusPembayaran;
import com.testproject.model.Transaksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LaporanService {

    public List<RiwayatRestock> getRestock(String dari, String sampai) {
        List<RiwayatRestock> list = new ArrayList<>();
        String sql = "SELECT * FROM riwayat_restock WHERE tanggal BETWEEN ? AND ? ORDER BY tanggal DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dari);
            stmt.setString(2, sampai);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new RiwayatRestock(
                        rs.getInt("id"), rs.getInt("bahan_id"), rs.getString("tanggal"),
                        rs.getDouble("jumlah_tambah"), rs.getDouble("harga_total"), rs.getDouble("harga_per_satuan")));
            }
        } catch (Exception e) { System.out.println("Error getRestock: " + e.getMessage()); }
        return list;
    }

    public List<Transaksi> getTransaksi(String dari, String sampai) {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT * FROM transaksi WHERE tanggal BETWEEN ? AND ? ORDER BY tanggal DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dari);
            stmt.setString(2, sampai);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Transaksi(
                        rs.getInt("id"), rs.getString("nama_pelanggan"), rs.getString("tanggal"),
                        rs.getDouble("total_harga"), rs.getString("metode_bayar"),
                        StatusPembayaran.valueOf(rs.getString("status_pembayaran"))));
            }
        } catch (Exception e) { System.out.println("Error getTransaksi: " + e.getMessage()); }
        return list;
    }

    public double getPenghasilan(String dari, String sampai) {
        double total = 0;
        String sql = "SELECT COALESCE(SUM(total_harga), 0) FROM transaksi WHERE tanggal BETWEEN ? AND ? AND status_pembayaran = 'LUNAS'";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dari);
            stmt.setString(2, sampai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) total = rs.getDouble(1);
        } catch (Exception e) { System.out.println("Error hitung penghasilan: " + e.getMessage()); }
        return total;
    }

    public double getPengeluaran(String dari, String sampai) {
        double total = 0;
        String sql = "SELECT COALESCE(SUM(harga_total), 0) FROM riwayat_restock WHERE tanggal BETWEEN ? AND ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dari);
            stmt.setString(2, sampai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) total = rs.getDouble(1);
        } catch (Exception e) { System.out.println("Error hitung pengeluaran: " + e.getMessage()); }
        return total;
    }

    public String getNamaBahan(int id) {
        String sql = "SELECT nama FROM bahan WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("nama");
        } catch (Exception e) { System.out.println("Error getNamaBahan: " + e.getMessage()); }
        return "Unknown";
    }
}