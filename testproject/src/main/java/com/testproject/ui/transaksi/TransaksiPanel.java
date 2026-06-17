package com.testproject.ui.transaksi;

import com.testproject.service.TransaksiService;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TransaksiPanel extends VBox {
    private final TransaksiService transaksiService = new TransaksiService();
    private final OrderPane orderPane;
    private final RiwayatTransaksiPane riwayatPane;

    public TransaksiPanel() {
        setPadding(new Insets(10));
        setSpacing(10);

        Label title = new Label("Mesin Kasir (POS)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        riwayatPane = new RiwayatTransaksiPane(transaksiService, () -> loadData());
        orderPane = new OrderPane(transaksiService, () -> riwayatPane.refreshData());

        // Menggunakan TabPane untuk menyembunyikan Riwayat dari pandangan utama Kasir
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab tabKasir = new Tab("🛒 Buka Kasir", orderPane);
        Tab tabRiwayat = new Tab("🧾 Riwayat Transaksi", riwayatPane);

        tabPane.getTabs().addAll(tabKasir, tabRiwayat);

        // Refresh riwayat saat tab Riwayat diklik
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldT, newT) -> {
            if (newT == tabRiwayat) riwayatPane.refreshData();
        });

        getChildren().addAll(title, tabPane);
        loadData();
    }

    public void loadData() {
        orderPane.loadMenuData();
        riwayatPane.refreshData();
    }
}