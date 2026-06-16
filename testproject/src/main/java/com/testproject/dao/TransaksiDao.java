package com.testproject.dao;

import com.testproject.db.DatabaseHelper;
import com.testproject.model.DetailTransaksi;
import com.testproject.model.StatusPembayaran;
import com.testproject.model.Transaksi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDao {

    private final BahanDao bahanDao = new BahanDao();

    public List<Transaksi> getAll() {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT * FROM transaksi ORDER BY id DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            System.out.println("Error getAll Transaksi: " + e.getMessage());
        }
        return list;
    }

    /**
     * Saves a full transaksi atomically:
     * INSERT transaksi → INSERT all detail_transaksi → deduct all bahan stock.
     * If anything fails the entire operation is rolled back.
     *
     * @return the new transaksi id, or -1 if the operation failed or stock was insufficient
     */
    public int simpanTransaksiLengkap(
            Transaksi t,
            List<DetailTransaksi> details,
            OpsiMenuDao opsiMenuDao,
            List<List<Integer>> checkedOpsiIdsList,
            List<List<Integer>> chosenPilihanIdsList) {

        String sqlTransaksi = "INSERT INTO transaksi " +
                "(nama_pelanggan, tanggal, total_harga, metode_bayar, status_pembayaran) " +
                "VALUES (?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO detail_transaksi " +
                "(transaksi_id, menu_id, jumlah, subtotal) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getTransactionConnection()) {
            try {
                // 1. Insert transaksi header
                int transaksiId;
                try (PreparedStatement stmt = conn.prepareStatement(
                        sqlTransaksi, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, t.getNamaPelanggan());
                    stmt.setString(2, t.getTanggal());
                    stmt.setDouble(3, t.getTotalHarga());
                    stmt.setString(4, t.getMetodeBayar());
                    stmt.setString(5, t.getStatusPembayaran().name());
                    stmt.executeUpdate();
                    ResultSet keys = stmt.getGeneratedKeys();
                    if (!keys.next()) throw new Exception("Gagal mendapatkan transaksi ID");
                    transaksiId = keys.getInt(1);
                }

                // 2. Insert all detail rows
                try (PreparedStatement stmt = conn.prepareStatement(sqlDetail)) {
                    for (DetailTransaksi d : details) {
                        stmt.setInt(1, transaksiId);
                        stmt.setInt(2, d.getMenuId());
                        stmt.setInt(3, d.getJumlah());
                        stmt.setDouble(4, d.getSubtotal());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }

                // 3. Deduct stock for each item (uses its own transaction internally,
                //    so we pre-check here using the shared connection)
                for (int i = 0; i < details.size(); i++) {
                    DetailTransaksi d = details.get(i);
                    boolean ok = bahanDao.kurangiStokWithOpsi(
                        opsiMenuDao,
                        d.getMenuId(),
                        d.getJumlah(),
                        checkedOpsiIdsList.get(i),
                        chosenPilihanIdsList.get(i)
                    );
                    if (!ok) {
                        conn.rollback();
                        System.out.println("Stok tidak cukup — transaksi dibatalkan.");
                        return -1;
                    }
                }

                conn.commit();
                return transaksiId;

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Error simpanTransaksiLengkap (rolled back): " + e.getMessage());
                return -1;
            }
        } catch (Exception e) {
            System.out.println("Error simpanTransaksiLengkap connection: " + e.getMessage());
            return -1;
        }
    }

    public int add(Transaksi t) {
        String sql = "INSERT INTO transaksi " +
                "(nama_pelanggan, tanggal, total_harga, metode_bayar, status_pembayaran) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, t.getNamaPelanggan());
            stmt.setString(2, t.getTanggal());
            stmt.setDouble(3, t.getTotalHarga());
            stmt.setString(4, t.getMetodeBayar());
            stmt.setString(5, t.getStatusPembayaran().name());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (Exception e) {
            System.out.println("Error add Transaksi: " + e.getMessage());
        }
        return -1;
    }

    public void updateStatus(int id, String status) {
        String sql = "UPDATE transaksi SET status_pembayaran=? WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            System.out.println("updateStatus rows affected: " + rows);
        } catch (Exception e) {
            System.out.println("Error updateStatus Transaksi: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sqlDetail = "DELETE FROM detail_transaksi WHERE transaksi_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlDetail)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error delete detail: " + e.getMessage());
        }

        String sql = "DELETE FROM transaksi WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error delete Transaksi: " + e.getMessage());
        }
    }

    public void addDetail(DetailTransaksi d) {
        String sql = "INSERT INTO detail_transaksi " +
                "(transaksi_id, menu_id, jumlah, subtotal) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, d.getTransaksiId());
            stmt.setInt(2, d.getMenuId());
            stmt.setInt(3, d.getJumlah());
            stmt.setDouble(4, d.getSubtotal());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error addDetail: " + e.getMessage());
        }
    }

    public List<DetailTransaksi> getDetailByTransaksiId(int transaksiId) {
        List<DetailTransaksi> list = new ArrayList<>();
        String sql = "SELECT * FROM detail_transaksi WHERE transaksi_id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transaksiId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new DetailTransaksi(
                    rs.getInt("id"), rs.getInt("transaksi_id"),
                    rs.getInt("menu_id"), rs.getInt("jumlah"),
                    rs.getDouble("subtotal")));
            }
        } catch (Exception e) {
            System.out.println("Error getDetail: " + e.getMessage());
        }
        return list;
    }

    private Transaksi mapRow(ResultSet rs) throws Exception {
        String statusStr = rs.getString("status_pembayaran");
        StatusPembayaran status;
        try { status = StatusPembayaran.valueOf(statusStr); }
        catch (Exception e) { status = StatusPembayaran.BELUM_LUNAS; }
        return new Transaksi(
            rs.getInt("id"), rs.getString("nama_pelanggan"),
            rs.getString("tanggal"), rs.getDouble("total_harga"),
            rs.getString("metode_bayar"), status);
    }
}