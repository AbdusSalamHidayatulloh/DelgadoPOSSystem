package com.testproject.ui.laporan;

import com.testproject.service.LaporanService;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class LaporanPanel extends VBox {

    private final LaporanService service = new LaporanService();
    private final TabPane tabPane = new TabPane();

    private final RestockReportPane restockPane;
    private final TransaksiReportPane transaksiPane;
    private final RekapReportPane rekapPane;

    public LaporanPanel() {
        setPadding(new Insets(20));
        setSpacing(15);

        Label title = new Label("Laporan Keuangan");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        restockPane = new RestockReportPane(service);
        transaksiPane = new TransaksiReportPane(service);
        rekapPane = new RekapReportPane(service);

        Tab tabRestock = new Tab("📦 Riwayat Restock", restockPane);
        Tab tabTransaksi = new Tab("🧾 Riwayat Transaksi", transaksiPane);
        Tab tabRekap = new Tab("📊 Rekap", rekapPane);

        tabPane.getTabs().addAll(tabRestock, tabTransaksi, tabRekap);

        // Load data secara otomatis ketika tab di-klik
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabRestock) restockPane.loadData();
            else if (newTab == tabTransaksi) transaksiPane.loadData();
            else if (newTab == tabRekap) rekapPane.loadData();
        });

        getChildren().addAll(title, tabPane);
        restockPane.loadData(); // Load tab pertama
    }

    public void loadData() {
        int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
        if (selectedIndex == 0) restockPane.loadData();
        else if (selectedIndex == 1) transaksiPane.loadData();
        else if (selectedIndex == 2) rekapPane.loadData();
    }
}