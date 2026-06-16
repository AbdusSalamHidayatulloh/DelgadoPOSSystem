package com.testproject.dao;

import com.testproject.db.DatabaseHelper;
import com.testproject.model.Bahan;
import com.testproject.model.ResepItem;
import com.testproject.model.RiwayatRestock;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BahanDao {

    public List<Bahan> getAll() {
        List<Bahan> list = new ArrayList<>();
        String sql = "SELECT * FROM bahan";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.out.println("Error getAll Bahan: " + e.getMessage());
        }
        return list;
    }

    public void add(Bahan b) {
        String sql = "INSERT INTO bahan (nama, satuan, jumlah, stok_minimum) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, b.getNama());
            stmt.setString(2, b.getSatuan());
            stmt.setDouble(3, b.getJumlah());
            stmt.setDouble(4, b.getStokMinimum());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error add Bahan: " + e.getMessage());
        }
    }

    public void update(Bahan b) {
        String sql = "UPDATE bahan SET nama=?, satuan=?, jumlah=?, stok_minimum=? WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, b.getNama());
            stmt.setString(2, b.getSatuan());
            stmt.setDouble(3, b.getJumlah());
            stmt.setDouble(4, b.getStokMinimum());
            stmt.setInt(5, b.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error update Bahan: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM bahan WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error delete Bahan: " + e.getMessage());
        }
    }

    /**
     * Atomically updates bahan stock AND inserts riwayat_restock.
     * Rolls back both if either fails.
     */
    public boolean restock(int bahanId, double jumlahTambah, double hargaTotal, String tanggal) {
        double hargaPerSatuan = hargaTotal / jumlahTambah;

        String sqlUpdate = "UPDATE bahan SET jumlah = jumlah + ? WHERE id=?";
        String sqlInsert = "INSERT INTO riwayat_restock " +
                           "(bahan_id, tanggal, jumlah_tambah, harga_total, harga_per_satuan) " +
                           "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getTransactionConnection()) {
            try {
                // 1. Update stock
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                    stmt.setDouble(1, jumlahTambah);
                    stmt.setInt(2, bahanId);
                    stmt.executeUpdate();
                }

                // 2. Insert history
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                    stmt.setInt(1, bahanId);
                    stmt.setString(2, tanggal);
                    stmt.setDouble(3, jumlahTambah);
                    stmt.setDouble(4, hargaTotal);
                    stmt.setDouble(5, hargaPerSatuan);
                    stmt.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Error restock (rolled back): " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error restock connection: " + e.getMessage());
            return false;
        }
    }

    public List<Bahan> getStokRendah() {
        List<Bahan> list = new ArrayList<>();
        String sql = "SELECT * FROM bahan WHERE jumlah <= stok_minimum";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            System.out.println("Error getStokRendah: " + e.getMessage());
        }
        return list;
    }

    public List<RiwayatRestock> getRiwayatRestock(int bahanId) {
        List<RiwayatRestock> list = new ArrayList<>();
        String sql = "SELECT * FROM riwayat_restock WHERE bahan_id=? ORDER BY tanggal DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bahanId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new RiwayatRestock(
                    rs.getInt("id"), rs.getInt("bahan_id"), rs.getString("tanggal"),
                    rs.getDouble("jumlah_tambah"), rs.getDouble("harga_total"),
                    rs.getDouble("harga_per_satuan")));
            }
        } catch (Exception e) {
            System.out.println("Error getRiwayatRestock: " + e.getMessage());
        }
        return list;
    }

    // ── RESEP MENU ───────────────────────────────────────────────

    public List<ResepItem> getResepByMenuId(int menuId) {
        List<ResepItem> list = new ArrayList<>();
        String sql = "SELECT r.menu_id, r.bahan_id, r.jumlah_dipakai, b.nama, b.satuan " +
                     "FROM resep_menu r JOIN bahan b ON r.bahan_id = b.id WHERE r.menu_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new ResepItem(
                    rs.getInt("menu_id"), rs.getInt("bahan_id"),
                    rs.getString("nama"), rs.getString("satuan"),
                    rs.getDouble("jumlah_dipakai")));
            }
        } catch (Exception e) {
            System.out.println("Error getResep: " + e.getMessage());
        }
        return list;
    }

    public void addResep(int menuId, int bahanId, double jumlahDipakai) {
        String sql = "INSERT INTO resep_menu (menu_id, bahan_id, jumlah_dipakai) VALUES (?, ?, ?) " +
                     "ON CONFLICT(menu_id, bahan_id) DO UPDATE SET jumlah_dipakai=excluded.jumlah_dipakai";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            stmt.setInt(2, bahanId);
            stmt.setDouble(3, jumlahDipakai);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error addResep: " + e.getMessage());
        }
    }

    public void deleteResep(int menuId, int bahanId) {
        String sql = "DELETE FROM resep_menu WHERE menu_id=? AND bahan_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            stmt.setInt(2, bahanId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error deleteResep: " + e.getMessage());
        }
    }

    // ── STOCK DEDUCTION ──────────────────────────────────────────

    /**
     * Atomically checks and deducts stock for a menu item + chosen options.
     * All checks and deductions run in a single transaction — either all
     * succeed or nothing is changed.
     */
    public boolean kurangiStokWithOpsi(
            OpsiMenuDao opsiMenuDao,
            int menuId, int jumlah,
            List<Integer> checkedOpsiIds,
            List<Integer> chosenPilihanIds) {

        // Build total needed map outside the transaction (read-only queries)
        Map<Integer, Double> totalNeeded = new LinkedHashMap<>();
        Map<Integer, String> namaMap    = new LinkedHashMap<>();

        for (ResepItem r : getResepByMenuId(menuId)) {
            totalNeeded.merge(r.getBahanId(), r.getJumlahDipakai() * jumlah, Double::sum);
            namaMap.put(r.getBahanId(), r.getNamaBahan());
        }
        for (int opsiId : checkedOpsiIds) {
            for (ResepItem r : opsiMenuDao.getResepByOpsiId(opsiId)) {
                totalNeeded.merge(r.getBahanId(), r.getJumlahDipakai() * jumlah, Double::sum);
                namaMap.put(r.getBahanId(), r.getNamaBahan());
            }
        }
        for (int pilihanId : chosenPilihanIds) {
            for (ResepItem r : opsiMenuDao.getResepByPilihanId(pilihanId)) {
                totalNeeded.merge(r.getBahanId(), r.getJumlahDipakai() * jumlah, Double::sum);
                namaMap.put(r.getBahanId(), r.getNamaBahan());
            }
        }

        if (totalNeeded.isEmpty()) return true;

        // Single transaction: check then deduct
        try (Connection conn = DatabaseHelper.getTransactionConnection()) {
            try {
                // 1. Check all sufficiency within the same connection/transaction
                String sqlCheck = "SELECT jumlah FROM bahan WHERE id=?";
                for (Map.Entry<Integer, Double> entry : totalNeeded.entrySet()) {
                    try (PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
                        stmt.setInt(1, entry.getKey());
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            double stokAda = rs.getDouble("jumlah");
                            if (stokAda < entry.getValue()) {
                                System.out.printf("Stok tidak cukup: %s (ada %.2f, butuh %.2f)%n",
                                    namaMap.get(entry.getKey()), stokAda, entry.getValue());
                                conn.rollback();
                                return false;
                            }
                        }
                    }
                }

                // 2. Deduct all
                String sqlDeduct = "UPDATE bahan SET jumlah = jumlah - ? WHERE id=?";
                for (Map.Entry<Integer, Double> entry : totalNeeded.entrySet()) {
                    try (PreparedStatement stmt = conn.prepareStatement(sqlDeduct)) {
                        stmt.setDouble(1, entry.getValue());
                        stmt.setInt(2, entry.getKey());
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Error kurangiStok (rolled back): " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error kurangiStok connection: " + e.getMessage());
            return false;
        }
    }

    /** Legacy wrapper — keeps TransaksiDao.addDetailWithStockDeduction compiling. */
    public boolean kurangiStok(int menuId, int jumlahDipesan) {
        return kurangiStokWithOpsi(new OpsiMenuDao(), menuId, jumlahDipesan,
                new ArrayList<>(), new ArrayList<>());
    }

    // ── HELPERS ──────────────────────────────────────────────────

    private Bahan mapRow(ResultSet rs) throws Exception {
        return new Bahan(
            rs.getInt("id"),
            rs.getString("nama"),
            rs.getString("satuan"),
            rs.getDouble("jumlah"),
            rs.getDouble("stok_minimum"));
    }
}