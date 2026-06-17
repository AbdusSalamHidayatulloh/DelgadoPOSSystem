package com.testproject.service;

import com.testproject.db.DatabaseHelper;
import com.testproject.model.Bahan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardService {
    
    public Map<String, Integer> getTop5MenuLaris() {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT m.nama, SUM(d.jumlah) as terjual FROM detail_transaksi d " +
                     "JOIN menu m ON d.menu_id = m.id " +
                     "JOIN transaksi t ON d.transaksi_id = t.id " +
                     "WHERE t.status_pembayaran = 'LUNAS' " +
                     "GROUP BY d.menu_id ORDER BY terjual DESC LIMIT 5";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) map.put(rs.getString("nama"), rs.getInt("terjual"));
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    public Map<String, Double> getPendapatan7HariTerakhir() {
        Map<String, Double> map = new HashMap<>();
        String sql = "SELECT date(tanggal) as tgl, SUM(total_harga) as pendapatan FROM transaksi " +
                     "WHERE status_pembayaran = 'LUNAS' AND date(tanggal) >= date('now', '-7 days') " +
                     "GROUP BY date(tanggal) ORDER BY tgl ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) map.put(rs.getString("tgl"), rs.getDouble("pendapatan"));
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    public List<Bahan> getBahanStokKritis() {
        List<Bahan> list = new ArrayList<>();
        String sql = "SELECT * FROM bahan WHERE jumlah <= stok_minimum";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Bahan(rs.getInt("id"), rs.getString("nama"), rs.getString("satuan"), 
                                   rs.getDouble("jumlah"), rs.getDouble("stok_minimum")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}