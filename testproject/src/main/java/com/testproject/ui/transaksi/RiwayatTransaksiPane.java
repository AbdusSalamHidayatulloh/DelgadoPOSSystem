package com.testproject.ui.transaksi;

import com.testproject.model.StatusPembayaran;
import com.testproject.model.Transaksi;
import com.testproject.service.TransaksiService;
import com.testproject.utils.UIHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RiwayatTransaksiPane extends VBox {
    private final TransaksiService service;
    private final Runnable onDataChanged;
    private final TableView<Transaksi> tableTransaksi = new TableView<>();
    private final ObservableList<Transaksi> dataTransaksi = FXCollections.observableArrayList();

    public RiwayatTransaksiPane(TransaksiService service, Runnable onDataChanged) {
        this.service = service;
        this.onDataChanged = onDataChanged;
        setSpacing(8);

        Label lbl = new Label("Riwayat Transaksi");
        lbl.setStyle("-fx-font-weight: bold;");

        TableColumn<Transaksi, Integer> colId = new TableColumn<>("ID"); colId.setCellValueFactory(new PropertyValueFactory<>("id")); colId.setPrefWidth(40);
        TableColumn<Transaksi, String> colTanggal = new TableColumn<>("Tanggal"); colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal")); colTanggal.setPrefWidth(125);
        TableColumn<Transaksi, String> colNama = new TableColumn<>("Pelanggan"); colNama.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan")); colNama.setPrefWidth(100);
        
        // --- KOLOM BARU UNTUK TIPE PESANAN ---
        TableColumn<Transaksi, String> colTipe = new TableColumn<>("Tipe"); colTipe.setCellValueFactory(new PropertyValueFactory<>("tipePesanan")); colTipe.setPrefWidth(85);

        TableColumn<Transaksi, Double> colTotal = new TableColumn<>("Total"); colTotal.setCellValueFactory(new PropertyValueFactory<>("totalHarga")); colTotal.setCellFactory(col -> new UIHelper.FormatDesimalCell<>()); colTotal.setPrefWidth(80);
        TableColumn<Transaksi, String> colMetode = new TableColumn<>("Metode"); colMetode.setCellValueFactory(new PropertyValueFactory<>("metodeBayar")); colMetode.setPrefWidth(65);

        TableColumn<Transaksi, StatusPembayaran> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusPembayaran"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(StatusPembayaran val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); } 
                else {
                    boolean lunas = val == StatusPembayaran.LUNAS;
                    setText(lunas ? "✓ Lunas" : "⏳ Belum");
                    setStyle(lunas ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
        colStatus.setPrefWidth(75);

        // Masukkan colTipe ke dalam tabel
        tableTransaksi.getColumns().addAll(colId, colTanggal, colNama, colTipe, colTotal, colMetode, colStatus);
        tableTransaksi.setItems(dataTransaksi);
        tableTransaksi.setPrefHeight(500);

        Button btnLunas = new Button("✔ Tandai Lunas");
        btnLunas.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnLunas.setOnAction(e -> {
            Transaksi selected = tableTransaksi.getSelectionModel().getSelectedItem();
            if (selected == null) { UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih transaksi dulu!"); return; }
            service.tandaiLunas(selected.getId());
            onDataChanged.run();
        });

        Button btnHapus = new Button("✖ Hapus");
        btnHapus.setOnAction(e -> {
            Transaksi selected = tableTransaksi.getSelectionModel().getSelectedItem();
            if (selected == null) { UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih transaksi dulu!"); return; }
            service.hapusTransaksi(selected.getId());
            onDataChanged.run();
        });

        getChildren().addAll(lbl, tableTransaksi, new HBox(8, btnLunas, btnHapus));
    }

    public void refreshData() {
        dataTransaksi.setAll(service.ambilSemuaTransaksi());
    }
}