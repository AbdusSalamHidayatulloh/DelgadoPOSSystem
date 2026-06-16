package com.testproject.dao;

import com.testproject.db.DatabaseHelper;
import com.testproject.model.OpsiMenu;
import com.testproject.model.PilihanOpsi;
import com.testproject.model.ResepItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OpsiMenuDao {

    public List<OpsiMenu> getByMenuId(int menuId) {
        List<OpsiMenu> list = new ArrayList<>();
        String sql = "SELECT * FROM opsi_menu WHERE menu_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new OpsiMenu(
                    rs.getInt("id"),
                    rs.getInt("menu_id"),
                    rs.getString("nama_opsi"),
                    rs.getString("tipe_opsi"),
                    rs.getInt("wajib") == 1,
                    rs.getDouble("harga_tambahan")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error getByMenuId OpsiMenu: " + e.getMessage());
        }
        return list;
    }

    public int add(OpsiMenu o) {
        String sql = "INSERT INTO opsi_menu (menu_id, nama_opsi, tipe_opsi, wajib, harga_tambahan) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, o.getMenuId());
            stmt.setString(2, o.getNamaOpsi());
            stmt.setString(3, o.getTipeOpsi());
            stmt.setInt(4, o.isWajib() ? 1 : 0);
            stmt.setDouble(5, o.getHargaTambahan());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (Exception e) {
            System.out.println("Error add OpsiMenu: " + e.getMessage());
        }
        return -1;
    }

    public void delete(int id) {
        // Delete resep_opsi first
        String sqlResepOpsi = "DELETE FROM resep_opsi WHERE opsi_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlResepOpsi)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error delete resep_opsi: " + e.getMessage());
        }

        // Delete all pilihan (and their resep) under this opsi
        List<PilihanOpsi> pilihanList = getPilihanByOpsiId(id);
        for (PilihanOpsi p : pilihanList) deletePilihan(p.getId());

        String sql = "DELETE FROM opsi_menu WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error delete OpsiMenu: " + e.getMessage());
        }
    }

    public void deleteByMenuId(int menuId) {
        List<OpsiMenu> opsiList = getByMenuId(menuId);
        for (OpsiMenu o : opsiList) delete(o.getId());
    }

    // ── PILIHAN OPSI ─────────────────────────────────────────────

    public List<PilihanOpsi> getPilihanByOpsiId(int opsiId) {
        List<PilihanOpsi> list = new ArrayList<>();
        String sql = "SELECT * FROM pilihan_opsi WHERE opsi_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, opsiId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new PilihanOpsi(
                    rs.getInt("id"),
                    rs.getInt("opsi_id"),
                    rs.getString("label"),
                    rs.getDouble("harga_tambahan")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error getPilihan: " + e.getMessage());
        }
        return list;
    }

    public void addPilihan(PilihanOpsi p) {
        String sql = "INSERT INTO pilihan_opsi (opsi_id, label, harga_tambahan) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, p.getOpsiId());
            stmt.setString(2, p.getLabel());
            stmt.setDouble(3, p.getHargaTambahan());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error addPilihan: " + e.getMessage());
        }
    }

    public void updatePilihan(PilihanOpsi p) {
        String sql = "UPDATE pilihan_opsi SET label=?, harga_tambahan=? WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getLabel());
            stmt.setDouble(2, p.getHargaTambahan());
            stmt.setInt(3, p.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error updatePilihan: " + e.getMessage());
        }
    }

    public void deletePilihan(int id) {
        // Clean resep_pilihan first
        String sqlResep = "DELETE FROM resep_pilihan WHERE pilihan_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlResep)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error delete resep_pilihan: " + e.getMessage());
        }

        String sql = "DELETE FROM pilihan_opsi WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error deletePilihan: " + e.getMessage());
        }
    }

    // ── RESEP OPSI (checkbox) ─────────────────────────────────────

    public List<ResepItem> getResepByOpsiId(int opsiId) {
        List<ResepItem> list = new ArrayList<>();
        String sql = "SELECT r.opsi_id, r.bahan_id, r.jumlah_dipakai, b.nama, b.satuan " +
                     "FROM resep_opsi r JOIN bahan b ON r.bahan_id = b.id WHERE r.opsi_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, opsiId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new ResepItem(
                    rs.getInt("opsi_id"),
                    rs.getInt("bahan_id"),
                    rs.getString("nama"),
                    rs.getString("satuan"),
                    rs.getDouble("jumlah_dipakai")));
            }
        } catch (Exception e) {
            System.out.println("Error getResepByOpsiId: " + e.getMessage());
        }
        return list;
    }

    public void addResepOpsi(int opsiId, int bahanId, double jumlah) {
        String sql = "INSERT INTO resep_opsi (opsi_id, bahan_id, jumlah_dipakai) VALUES (?, ?, ?) " +
                     "ON CONFLICT(opsi_id, bahan_id) DO UPDATE SET jumlah_dipakai=excluded.jumlah_dipakai";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, opsiId);
            stmt.setInt(2, bahanId);
            stmt.setDouble(3, jumlah);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error addResepOpsi: " + e.getMessage());
        }
    }

    public void deleteResepOpsi(int opsiId, int bahanId) {
        String sql = "DELETE FROM resep_opsi WHERE opsi_id=? AND bahan_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, opsiId);
            stmt.setInt(2, bahanId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error deleteResepOpsi: " + e.getMessage());
        }
    }

    // ── RESEP PILIHAN ─────────────────────────────────────────────

    public List<ResepItem> getResepByPilihanId(int pilihanId) {
        List<ResepItem> list = new ArrayList<>();
        String sql = "SELECT r.pilihan_id, r.bahan_id, r.jumlah_dipakai, b.nama, b.satuan " +
                     "FROM resep_pilihan r JOIN bahan b ON r.bahan_id = b.id WHERE r.pilihan_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pilihanId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new ResepItem(
                    rs.getInt("pilihan_id"),
                    rs.getInt("bahan_id"),
                    rs.getString("nama"),
                    rs.getString("satuan"),
                    rs.getDouble("jumlah_dipakai")));
            }
        } catch (Exception e) {
            System.out.println("Error getResepByPilihanId: " + e.getMessage());
        }
        return list;
    }

    public void addResepPilihan(int pilihanId, int bahanId, double jumlah) {
        String sql = "INSERT INTO resep_pilihan (pilihan_id, bahan_id, jumlah_dipakai) VALUES (?, ?, ?) " +
                     "ON CONFLICT(pilihan_id, bahan_id) DO UPDATE SET jumlah_dipakai=excluded.jumlah_dipakai";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pilihanId);
            stmt.setInt(2, bahanId);
            stmt.setDouble(3, jumlah);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error addResepPilihan: " + e.getMessage());
        }
    }

    public void deleteResepPilihan(int pilihanId, int bahanId) {
        String sql = "DELETE FROM resep_pilihan WHERE pilihan_id=? AND bahan_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pilihanId);
            stmt.setInt(2, bahanId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error deleteResepPilihan: " + e.getMessage());
        }
    }
}