package com.testproject.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:sqlite:stall.db";

    public static Connection getConnection() throws Exception {
        Connection conn = DriverManager.getConnection(DB_URL);
        // Enable foreign key enforcement on every connection
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    /**
     * Returns a connection with auto-commit OFF for manual transaction control.
     * Caller is responsible for calling commit() or rollback() then close().
     */
    public static Connection getTransactionConnection() throws Exception {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS bahan (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nama TEXT NOT NULL,
                            satuan TEXT NOT NULL,
                            jumlah REAL NOT NULL,
                            stok_minimum REAL NOT NULL DEFAULT 0
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS riwayat_restock (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            bahan_id INTEGER NOT NULL,
                            tanggal TEXT NOT NULL,
                            jumlah_tambah REAL NOT NULL,
                            harga_total REAL NOT NULL,
                            harga_per_satuan REAL NOT NULL,
                            FOREIGN KEY (bahan_id) REFERENCES bahan(id)
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS menu (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nama TEXT NOT NULL,
                            harga REAL NOT NULL,
                            tipe TEXT NOT NULL
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS resep_menu (
                            menu_id INTEGER NOT NULL,
                            bahan_id INTEGER NOT NULL,
                            jumlah_dipakai REAL NOT NULL,
                            PRIMARY KEY (menu_id, bahan_id),
                            FOREIGN KEY (menu_id) REFERENCES menu(id),
                            FOREIGN KEY (bahan_id) REFERENCES bahan(id)
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS opsi_menu (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            menu_id INTEGER NOT NULL,
                            nama_opsi TEXT NOT NULL,
                            tipe_opsi TEXT NOT NULL,
                            wajib INTEGER NOT NULL DEFAULT 0,
                            harga_tambahan REAL NOT NULL DEFAULT 0,
                            FOREIGN KEY (menu_id) REFERENCES menu(id)
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS pilihan_opsi (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            opsi_id INTEGER NOT NULL,
                            label TEXT NOT NULL,
                            harga_tambahan REAL NOT NULL DEFAULT 0,
                            FOREIGN KEY (opsi_id) REFERENCES opsi_menu(id)
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS transaksi (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nama_pelanggan TEXT,
                            tanggal TEXT NOT NULL,
                            total_harga REAL NOT NULL,
                            metode_bayar TEXT NOT NULL,
                            status_pembayaran TEXT NOT NULL DEFAULT 'BELUM_LUNAS'
                        )
                    """);
            try {
                stmt.execute("ALTER TABLE transaksi ADD COLUMN tipe_pesanan TEXT DEFAULT 'Dine-in'");
            } catch (Exception ignored) {}
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS detail_transaksi (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            transaksi_id INTEGER NOT NULL,
                            menu_id INTEGER NOT NULL,
                            jumlah INTEGER NOT NULL,
                            subtotal REAL NOT NULL,
                            keterangan TEXT DEFAULT '',
                            FOREIGN KEY (transaksi_id) REFERENCES transaksi(id),
                            FOREIGN KEY (menu_id) REFERENCES menu(id)
                        )
                    """);
            try {
                stmt.execute("ALTER TABLE detail_transaksi ADD COLUMN keterangan TEXT DEFAULT ''");
            } catch (Exception ignored) {}

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS shift_kas (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            tanggal_buka TEXT NOT NULL,
                            tanggal_tutup TEXT,
                            modal_awal REAL NOT NULL DEFAULT 0,
                            total_penjualan REAL NOT NULL DEFAULT 0,
                            total_kas REAL NOT NULL DEFAULT 0
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS resep_opsi (
                            opsi_id INTEGER NOT NULL,
                            bahan_id INTEGER NOT NULL,
                            jumlah_dipakai REAL NOT NULL,
                            PRIMARY KEY (opsi_id, bahan_id),
                            FOREIGN KEY (opsi_id) REFERENCES opsi_menu(id),
                            FOREIGN KEY (bahan_id) REFERENCES bahan(id)
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS resep_pilihan (
                            pilihan_id INTEGER NOT NULL,
                            bahan_id INTEGER NOT NULL,
                            jumlah_dipakai REAL NOT NULL,
                            PRIMARY KEY (pilihan_id, bahan_id),
                            FOREIGN KEY (pilihan_id) REFERENCES pilihan_opsi(id),
                            FOREIGN KEY (bahan_id) REFERENCES bahan(id)
                        )
                    """);

            System.out.println("Database initialized.");

        } catch (Exception e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }
}