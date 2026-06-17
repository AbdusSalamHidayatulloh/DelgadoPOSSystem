package com.testproject.ui.menu;

import com.testproject.model.Bahan;
import com.testproject.model.MenuItem;
import com.testproject.model.ResepItem;
import com.testproject.service.MenuService;
import com.testproject.utils.UIHelper;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class MenuResepTab extends VBox {
    private final MenuService service;
    private final TableView<ResepItem> tableResep = new TableView<>();
    private final ComboBox<Bahan> cmbBahanResep = new ComboBox<>();
    private final TextField txtJumlahResep = new TextField();
    
    // UI Untuk Kalkulator Keuntungan
    private final Label lblHargaJual = new Label("Harga Jual: Rp 0");
    private final Label lblTotalModal = new Label("Total Modal Bahan: Rp 0");
    private final Label lblProfit = new Label("Estimasi Keuntungan: Rp 0");

    private MenuItem currentMenu = null;

    public MenuResepTab(MenuService service) {
        this.service = service;
        setPadding(new Insets(10));
        setSpacing(10);

        Label lbl = new Label("Resep Menu (Bahan Dasar)");
        lbl.setStyle("-fx-font-weight: bold;");

        TableColumn<ResepItem, String> colBahan = new TableColumn<>("Bahan");
        colBahan.setCellValueFactory(new PropertyValueFactory<>("namaBahan"));
        colBahan.setPrefWidth(140);

        TableColumn<ResepItem, Double> colJumlah = new TableColumn<>("Jumlah Pakai");
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahDipakai"));
        colJumlah.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colJumlah.setPrefWidth(100);

        TableColumn<ResepItem, String> colSatuan = new TableColumn<>("Satuan");
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colSatuan.setPrefWidth(80);

        tableResep.getColumns().addAll(colBahan, colJumlah, colSatuan);
        tableResep.setPrefHeight(200);

        cmbBahanResep.setPromptText("Pilih bahan...");
        txtJumlahResep.setPromptText("Jumlah per porsi");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.addRow(0, new Label("Bahan:"), cmbBahanResep);
        grid.addRow(1, new Label("Jumlah:"), txtJumlahResep);

        Button btnTambah = new Button("Tambah ke Resep");
        Button btnHapus = new Button("Hapus dari Resep");

        btnTambah.setOnAction(e -> {
            if (currentMenu == null) { UIHelper.showAlert(Alert.AlertType.WARNING, "Pilih Menu", "Pilih menu di kiri dulu!"); return; }
            try {
                service.tambahResepMenu(currentMenu.getId(), cmbBahanResep.getValue(), txtJumlahResep.getText());
                refreshResep(); txtJumlahResep.clear(); cmbBahanResep.setValue(null);
            } catch (IllegalArgumentException ex) { UIHelper.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        });

        btnHapus.setOnAction(e -> {
            ResepItem sel = tableResep.getSelectionModel().getSelectedItem();
            if (sel != null && currentMenu != null) {
                service.hapusResepMenu(sel.getMenuId(), sel.getBahanId());
                refreshResep();
            }
        });

        // KARTU RINGKASAN KEUNTUNGAN
        VBox summaryCard = new VBox(6);
        summaryCard.setPadding(new Insets(10));
        summaryCard.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        
        lblHargaJual.setStyle("-fx-font-size: 13px;");
        lblTotalModal.setStyle("-fx-font-size: 13px;");
        lblProfit.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        summaryCard.getChildren().addAll(lblHargaJual, lblTotalModal, new Separator(), lblProfit);

        getChildren().addAll(lbl, tableResep, new Label("Tambah Bahan:"), grid, new HBox(8, btnTambah, btnHapus), new Separator(), summaryCard);
    }

    public void setMenu(MenuItem menu) {
        this.currentMenu = menu;
        refreshResep();
    }

    public void refreshBahanList(List<Bahan> bahans) {
        cmbBahanResep.getItems().setAll(bahans);
    }

    private void refreshResep() {
        if (currentMenu != null) {
            tableResep.getItems().setAll(service.ambilResepMenu(currentMenu.getId()));
            
            // --- LOGIKA PERHITUNGAN PREVIEW KEUNTUNGAN ---
            double hargaJual = currentMenu.getHarga();
            double totalModal = service.hitungTotalModalMenu(currentMenu.getId());
            double profit = hargaJual - totalModal;

            lblHargaJual.setText(String.format("Harga Jual Menu: Rp %.0f", hargaJual));
            lblTotalModal.setText(String.format("Total Modal Bahan (HPP): Rp %.0f", totalModal));
            lblProfit.setText(String.format("Estimasi Keuntungan: Rp %.0f", profit));

            // Beri warna merah jika rugi, hijau jika untung
            if (profit < 0) {
                lblProfit.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            } else {
                lblProfit.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
            }

        } else {
            tableResep.getItems().clear();
            lblHargaJual.setText("Harga Jual: Rp 0");
            lblTotalModal.setText("Total Modal Bahan: Rp 0");
            lblProfit.setText("Estimasi Keuntungan: Rp 0");
            lblProfit.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
        }
    }
}