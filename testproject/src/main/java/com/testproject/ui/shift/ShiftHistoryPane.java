package com.testproject.ui.shift;

import com.testproject.model.ShiftKas;
import com.testproject.utils.UIHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.List;

public class ShiftHistoryPane extends VBox {

    private final TableView<ShiftKas> tableShift = new TableView<>();
    private final ObservableList<ShiftKas> dataShift = FXCollections.observableArrayList();

    public ShiftHistoryPane() {
        setSpacing(8);
        setPrefWidth(640);

        Label lbl = new Label("Riwayat Shift");
        lbl.setStyle("-fx-font-weight: bold;");

        TableColumn<ShiftKas, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(40);

        TableColumn<ShiftKas, String> colBuka = new TableColumn<>("Buka");
        colBuka.setCellValueFactory(new PropertyValueFactory<>("tanggalBuka"));
        colBuka.setPrefWidth(130);

        TableColumn<ShiftKas, String> colTutup = new TableColumn<>("Tutup");
        colTutup.setCellValueFactory(new PropertyValueFactory<>("tanggalTutup"));
        colTutup.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                
                // PERBAIKAN: Jika barisnya memang kosong, bersihkan tulisannya
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } 
                // Jika baris ada isinya tapi tanggal tutupnya null/kosong, berarti Aktif
                else if (val == null || val.trim().isEmpty() || val.equals("null")) {
                    setText("(Aktif)");
                    setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } 
                // Jika sudah ada tanggal tutupnya
                else {
                    setText(val);
                    setStyle("");
                }
            }
        });
        colTutup.setPrefWidth(130);

        TableColumn<ShiftKas, Double> colModal = new TableColumn<>("Modal");
        colModal.setCellValueFactory(new PropertyValueFactory<>("modalAwal"));
        colModal.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colModal.setPrefWidth(100);

        TableColumn<ShiftKas, Double> colPenjualan = new TableColumn<>("Penjualan");
        colPenjualan.setCellValueFactory(new PropertyValueFactory<>("totalPenjualan"));
        colPenjualan.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colPenjualan.setPrefWidth(110);

        TableColumn<ShiftKas, Double> colKas = new TableColumn<>("Total Kas");
        colKas.setCellValueFactory(new PropertyValueFactory<>("totalKas"));
        colKas.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %.0f", val));
                setStyle(empty ? "" : "-fx-font-weight: bold;");
            }
        });
        colKas.setPrefWidth(110);

        tableShift.getColumns().addAll(colId, colBuka, colTutup, colModal, colPenjualan, colKas);
        tableShift.setItems(dataShift);

        getChildren().addAll(lbl, tableShift);
    }

    public void refreshData(List<ShiftKas> list) {
        dataShift.setAll(list);
    }
}