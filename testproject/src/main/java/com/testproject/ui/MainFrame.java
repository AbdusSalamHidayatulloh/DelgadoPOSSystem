package com.testproject.ui;

import com.testproject.db.DatabaseHelper;

import com.testproject.ui.dashboard.DashboardPanel;
import com.testproject.ui.bahan.BahanPanel;
import com.testproject.ui.menu.MenuPanel;
import com.testproject.ui.transaksi.TransaksiPanel;
import com.testproject.ui.shift.ShiftPanel;
import com.testproject.ui.laporan.LaporanPanel;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainFrame extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseHelper.initDatabase();

        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(20, 15, 20, 15));
        sidebar.setStyle("-fx-background-color: #2b2b2b; -fx-min-width: 170px;");

        // --- KELOMPOK 1: ANALITIK & LAPORAN (Di Puncak) ---
        Label lblAnalitik = new Label("ANALITIK & LAPORAN");
        // Padding atas dihilangkan karena ini elemen paling pertama
        lblAnalitik.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px; -fx-font-weight: bold;"); 
        Button btnBeranda = createMenuButton("📊 Dashboard");
        Button btnLaporan = createMenuButton("📈 Laporan Keuangan");

        // --- KELOMPOK 2: OPERASIONAL HARIAN ---
        Label lblOps = new Label("OPERASIONAL");
        // Ditambahkan padding atas (15px) agar tidak menempel dengan tombol di atasnya
        lblOps.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 15px 0 0 0;"); 
        Button btnShift = createMenuButton("💰 Shift & Kas");
        Button btnTransaksi = createMenuButton("🛒 Mesin Kasir");

        // --- KELOMPOK 3: MASTER DATA ---
        Label lblMaster = new Label("MASTER DATA");
        lblMaster.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 15px 0 0 0;");
        Button btnMenu = createMenuButton("🍔 Menu & Resep");
        Button btnBahan = createMenuButton("📦 Bahan Baku");

        BorderPane root = new BorderPane();
        
        // Instansiasi Panel
        DashboardPanel dashboardPanel = new DashboardPanel();
        BahanPanel bahanPanel = new BahanPanel();
        MenuPanel menuPanel = new MenuPanel();
        TransaksiPanel transaksiPanel = new TransaksiPanel();
        ShiftPanel shiftPanel = new ShiftPanel();
        LaporanPanel laporanPanel = new LaporanPanel();

        // Set Tampilan Default
        root.setCenter(dashboardPanel); 

        // Aksi Tombol Navigasi
        btnBeranda.setOnAction(e -> { root.setCenter(dashboardPanel); dashboardPanel.loadData(); });
        btnLaporan.setOnAction(e -> { root.setCenter(laporanPanel); laporanPanel.loadData(); });
        btnShift.setOnAction(e -> { root.setCenter(shiftPanel); shiftPanel.loadData(); });
        btnTransaksi.setOnAction(e -> { root.setCenter(transaksiPanel); transaksiPanel.loadData(); });
        btnMenu.setOnAction(e -> { root.setCenter(menuPanel); menuPanel.loadData(); });
        btnBahan.setOnAction(e -> { root.setCenter(bahanPanel); bahanPanel.loadData(); });

        // Menyusun urutan aktual di dalam panel kiri (Sidebar)
        sidebar.getChildren().addAll(
            lblAnalitik, btnBeranda, btnLaporan,
            lblOps, btnShift, btnTransaksi, 
            lblMaster, btnMenu, btnBahan
        );
        
        root.setLeft(sidebar);

        Scene scene = new Scene(root, 1150, 720); 
        stage.setTitle("Stall Manager (POS System)");
        stage.setScene(scene);
        stage.show();
    }

    // Fungsi pembantu agar kode tombol lebih rapi
    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: CENTER-LEFT; -fx-padding: 8px 5px;");
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}