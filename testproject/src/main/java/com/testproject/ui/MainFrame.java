package com.testproject.ui;

import com.testproject.db.DatabaseHelper;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainFrame extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseHelper.initDatabase();

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #2b2b2b; -fx-min-width: 150px;");

        Button btnBahan = new Button("🥦 Bahan");
        Button btnMenu = new Button("🍱 Menu");
        Button btnTransaksi = new Button("🧾 Transaksi");
        Button btnShift = new Button("💰 Shift/Kas");
        Button btnLaporan = new Button("📊 Laporan");

        for (Button btn : new Button[]{btnBahan, btnMenu, btnTransaksi, btnShift, btnLaporan}) {
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                         "-fx-font-size: 14px; -fx-alignment: CENTER-LEFT;");
        }

        BorderPane root = new BorderPane();
        BahanPanel bahanPanel = new BahanPanel();
        MenuPanel menuPanel = new MenuPanel();
        TransaksiPanel transaksiPanel = new TransaksiPanel();
        ShiftPanel shiftPanel = new ShiftPanel();
        LaporanPanel laporanPanel = new LaporanPanel();

        root.setCenter(bahanPanel);

        btnBahan.setOnAction(e -> { root.setCenter(bahanPanel); bahanPanel.loadData(); });
        btnMenu.setOnAction(e -> { root.setCenter(menuPanel); menuPanel.loadData(); });
        btnTransaksi.setOnAction(e -> { root.setCenter(transaksiPanel); transaksiPanel.loadData(); });
        btnShift.setOnAction(e -> { root.setCenter(shiftPanel); shiftPanel.loadData(); });
        btnLaporan.setOnAction(e -> { root.setCenter(laporanPanel); laporanPanel.loadData(); });

        sidebar.getChildren().addAll(btnBahan, btnMenu, btnTransaksi, btnShift, btnLaporan);
        root.setLeft(sidebar);

        Scene scene = new Scene(root, 950, 640);
        stage.setTitle("Stall Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}