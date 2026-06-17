package com.testproject.ui.bahan;

import com.testproject.model.Bahan;
import com.testproject.model.RiwayatRestock;
import com.testproject.service.BahanService;
import com.testproject.utils.UIHelper;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class RiwayatPanel extends VBox {
    private final BahanService service;
    private final TableView<RiwayatRestock> table = new TableView<>();
    private final Label lblTitle = new Label("Riwayat Restock (Pilih bahan)");

    public RiwayatPanel(BahanService service) {
        this.service = service;
        
        setPrefWidth(350);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 0 0 0 1;");
        lblTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TableColumn<RiwayatRestock, String> colTanggal = new TableColumn<>("Tanggal");
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colTanggal.setPrefWidth(120);

        TableColumn<RiwayatRestock, Double> colJumlah = new TableColumn<>("Jumlah");
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahTambah"));
        colJumlah.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colJumlah.setPrefWidth(70);

        TableColumn<RiwayatRestock, Double> colBiaya = new TableColumn<>("Biaya Total");
        colBiaya.setCellValueFactory(new PropertyValueFactory<>("hargaTotal"));
        colBiaya.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText((empty || val == null) ? null : String.format("Rp %.0f", val));
            }
        });
        colBiaya.setPrefWidth(100);

        table.getColumns().addAll(colTanggal, colJumlah, colBiaya);
        getChildren().addAll(lblTitle, table);
    }

    public void tampilkanRiwayat(Bahan bahan) {
        if (bahan != null) {
            lblTitle.setText("Riwayat Restock: " + bahan.getNama());
            table.getItems().setAll(service.ambilRiwayatRestock(bahan.getId()));
        }
    }

    public void clearRiwayat() {
        lblTitle.setText("Riwayat Restock (Pilih bahan)");
        table.getItems().clear();
    }
}