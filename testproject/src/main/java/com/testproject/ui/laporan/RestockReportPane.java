package com.testproject.ui.laporan;

import com.testproject.model.RiwayatRestock;
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

public class RestockReportPane extends VBox {
    private final LaporanService service;
    private final TableView<RiwayatRestock> table = new TableView<>();
    private final ObservableList<RiwayatRestock> data = FXCollections.observableArrayList();
    private final DatePicker dpDari = new DatePicker(LocalDate.now().minusMonths(1));
    private final DatePicker dpSampai = new DatePicker(LocalDate.now());
    private final Label lblTotal = new Label("Total Pengeluaran: Rp 0");

    public RestockReportPane(LaporanService service) {
        this.service = service;
        setPadding(new Insets(10));
        setSpacing(10);

        Button btnFilter = new Button("🔍 Filter");
        btnFilter.setOnAction(e -> loadData());
        HBox filter = new HBox(10, new Label("Dari:"), dpDari, new Label("Sampai:"), dpSampai, btnFilter);
        filter.setPadding(new Insets(10, 0, 10, 0));

        TableColumn<RiwayatRestock, String> colTanggal = new TableColumn<>("Tanggal"); colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal")); colTanggal.setPrefWidth(100);
        TableColumn<RiwayatRestock, Integer> colBahan = new TableColumn<>("Bahan"); colBahan.setCellValueFactory(new PropertyValueFactory<>("bahanId"));
        colBahan.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                setText(empty || id == null ? null : service.getNamaBahan(id));
            }
        });
        colBahan.setPrefWidth(130);

        TableColumn<RiwayatRestock, Double> colJumlah = new TableColumn<>("Jumlah"); colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahTambah")); colJumlah.setCellFactory(c -> new UIHelper.FormatDesimalCell<>()); colJumlah.setPrefWidth(80);
        TableColumn<RiwayatRestock, Double> colHargaTotal = new TableColumn<>("Harga Total"); colHargaTotal.setCellValueFactory(new PropertyValueFactory<>("hargaTotal")); colHargaTotal.setCellFactory(c -> new UIHelper.FormatDesimalCell<>()); colHargaTotal.setPrefWidth(120);
        TableColumn<RiwayatRestock, Double> colHargaSatuan = new TableColumn<>("Harga/Satuan"); colHargaSatuan.setCellValueFactory(new PropertyValueFactory<>("hargaPerSatuan")); colHargaSatuan.setCellFactory(c -> new UIHelper.FormatDesimalCell<>()); colHargaSatuan.setPrefWidth(120);

        table.getColumns().addAll(colTanggal, colBahan, colJumlah, colHargaTotal, colHargaSatuan);
        table.setItems(data);

        lblTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        data.addListener((ListChangeListener<RiwayatRestock>) c -> {
            double total = data.stream().mapToDouble(RiwayatRestock::getHargaTotal).sum();
            lblTotal.setText(String.format("Total Pengeluaran: Rp %.0f", total));
        });

        getChildren().addAll(filter, table, lblTotal);
    }

    public void loadData() {
        if (dpDari.getValue() != null && dpSampai.getValue() != null) {
            data.setAll(service.getRestock(dpDari.getValue().toString(), dpSampai.getValue().toString()));
        }
    }
}