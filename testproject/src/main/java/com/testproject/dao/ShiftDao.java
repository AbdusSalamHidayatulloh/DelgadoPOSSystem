package com.testproject.dao;

import com.testproject.db.DatabaseHelper;
import com.testproject.model.ShiftKas;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ShiftDao {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Open a new shift. Returns the new shift id, or -1 on failure. */
    public int bukaShift(double modalAwal) {
        String tanggal = LocalDateTime.now().format(FMT);
        String sql = "INSERT INTO shift_kas (tanggal_buka, modal_awal, total_penjualan, total_kas) " +
                     "VALUES (?, ?, 0, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tanggal);
            stmt.setDouble(2, modalAwal);
            stmt.setDouble(3, modalAwal);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (Exception e) {
            System.out.println("Error bukaShift: " + e.getMessage());
        }
        return -1;
    }

    /** Close the given shift. Recalculates total_penjualan from LUNAS transaksi. */
    public void tutupShift(int shiftId) {
        ShiftKas shift = getById(shiftId);
        if (shift == null) return;

        // Sum all LUNAS transactions created after shift opened
        double totalPenjualan = 0;
        String sqlSum = "SELECT COALESCE(SUM(total_harga), 0) FROM transaksi " +
                        "WHERE status_pembayaran = 'LUNAS' AND tanggal >= ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlSum)) {
            stmt.setString(1, shift.getTanggalBuka());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) totalPenjualan = rs.getDouble(1);
        } catch (Exception e) {
            System.out.println("Error sum penjualan: " + e.getMessage());
        }

        String tanggalTutup = LocalDateTime.now().format(FMT);
        double totalKas = shift.getModalAwal() + totalPenjualan;

        String sql = "UPDATE shift_kas SET tanggal_tutup=?, total_penjualan=?, total_kas=? WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tanggalTutup);
            stmt.setDouble(2, totalPenjualan);
            stmt.setDouble(3, totalKas);
            stmt.setInt(4, shiftId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error tutupShift: " + e.getMessage());
        }
    }

    /** Returns the currently open shift, or null if none. */
    public ShiftKas getShiftAktif() {
        String sql = "SELECT * FROM shift_kas WHERE tanggal_tutup IS NULL ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            System.out.println("Error getShiftAktif: " + e.getMessage());
        }
        return null;
    }

    public ShiftKas getById(int id) {
        String sql = "SELECT * FROM shift_kas WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            System.out.println("Error getShiftById: " + e.getMessage());
        }
        return null;
    }

    public List<ShiftKas> getAll() {
        List<ShiftKas> list = new ArrayList<>();
        String sql = "SELECT * FROM shift_kas ORDER BY id DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            System.out.println("Error getAll Shift: " + e.getMessage());
        }
        return list;
    }

    private ShiftKas mapRow(ResultSet rs) throws Exception {
        return new ShiftKas(
            rs.getInt("id"),
            rs.getString("tanggal_buka"),
            rs.getString("tanggal_tutup"),
            rs.getDouble("modal_awal"),
            rs.getDouble("total_penjualan"),
            rs.getDouble("total_kas")
        );
    }
}