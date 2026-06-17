package com.testproject.ui.laporan;

import com.testproject.model.StatusPembayaran;
import com.testproject.model.Transaksi;
import com.testproject.service.LaporanService;
import com.testproject.utils.UIHelper;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class TransaksiReportPane extends VBox {
    private final LaporanService service;
    private final TableView<Transaksi> table = new TableView<>();
    private final ObservableList<Transaksi> data = FXCollections.observableArrayList();
    private final DatePicker dpDari = new DatePicker(LocalDate.now().minusMonths(1));
    private final DatePicker dpSampai = new DatePicker(LocalDate.now());
    private final Label lblTotal = new Label("Total Penghasilan: Rp 0");

    public TransaksiReportPane(LaporanService service) {
        this.service = service;
        setPadding(new Insets(10));
        setSpacing(10);

        Button btnFilter = new Button("🔍 Filter");
        btnFilter.setOnAction(e -> loadData());
        HBox filter = new HBox(10, new Label("Dari:"), dpDari, new Label("Sampai:"), dpSampai, btnFilter);
        filter.setPadding(new Insets(10, 0, 10, 0));

        TableColumn<Transaksi, Integer> colId = new TableColumn<>("ID"); colId.setCellValueFactory(new PropertyValueFactory<>("id")); colId.setPrefWidth(50);
        TableColumn<Transaksi, String> colTanggal = new TableColumn<>("Tanggal"); colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal")); colTanggal.setPrefWidth(120);
        TableColumn<Transaksi, String> colNama = new TableColumn<>("Pelanggan"); colNama.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan")); colNama.setPrefWidth(120);
        TableColumn<Transaksi, Double> colTotal = new TableColumn<>("Total"); colTotal.setCellValueFactory(new PropertyValueFactory<>("totalHarga")); colTotal.setCellFactory(c -> new UIHelper.FormatDesimalCell<>()); colTotal.setPrefWidth(120);
        TableColumn<Transaksi, String> colMetode = new TableColumn<>("Metode"); colMetode.setCellValueFactory(new PropertyValueFactory<>("metodeBayar")); colMetode.setPrefWidth(100);

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
        colStatus.setPrefWidth(80);

        table.getColumns().addAll(colId, colTanggal, colNama, colTotal, colMetode, colStatus);
        table.setItems(data);

        lblTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        data.addListener((ListChangeListener<Transaksi>) c -> {
            double total = data.stream().mapToDouble(Transaksi::getTotalHarga).sum();
            lblTotal.setText(String.format("Total Penghasilan: Rp %.0f", total));
        });

        getChildren().addAll(filter, table, lblTotal);
    }

    public void loadData() {
        if (dpDari.getValue() != null && dpSampai.getValue() != null) {
            data.setAll(service.getTransaksi(dpDari.getValue().toString(), dpSampai.getValue().toString()));
        }
    }
}