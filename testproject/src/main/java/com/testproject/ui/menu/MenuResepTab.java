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

        getChildren().addAll(lbl, tableResep, new Label("Tambah Bahan:"), grid, new HBox(8, btnTambah, btnHapus));
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
        } else {
            tableResep.getItems().clear();
        }
    }
}