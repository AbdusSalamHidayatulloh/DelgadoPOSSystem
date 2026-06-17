package com.testproject.ui.menu;

import com.testproject.model.MenuItem;
import com.testproject.service.MenuService;
import com.testproject.utils.UIHelper;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class MenuLeftPane extends VBox {
    private final MenuService service;
    private final TableView<MenuItem> tableMenu = new TableView<>();
    private final TextField txtNama = new TextField();
    private final TextField txtHarga = new TextField();
    private final ComboBox<String> cmbTipe = new ComboBox<>();
    
    private MenuItem selectedMenu = null;

    public MenuLeftPane(MenuService service, Consumer<MenuItem> onMenuSelected) {
        this.service = service;
        setSpacing(8);
        setPrefWidth(350);

        Label lbl = new Label("Daftar Menu");
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Tabel Menu
        TableColumn<MenuItem, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNama.setPrefWidth(120);

        TableColumn<MenuItem, Double> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colHarga.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colHarga.setPrefWidth(90);

        TableColumn<MenuItem, String> colTipe = new TableColumn<>("Tipe");
        colTipe.setCellValueFactory(new PropertyValueFactory<>("tipe"));
        colTipe.setPrefWidth(80);

        tableMenu.getColumns().addAll(colNama, colHarga, colTipe);
        tableMenu.setPrefHeight(250);

        tableMenu.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            selectedMenu = n;
            if (n != null) {
                txtNama.setText(n.getNama());
                txtHarga.setText(String.valueOf(n.getHarga() == Math.floor(n.getHarga()) ? (int) n.getHarga() : n.getHarga()));
                cmbTipe.setValue(n.getTipe());
            }
            onMenuSelected.accept(n); // Memberitahu Tab Kanan
        });

        // Form Input
        txtNama.setPromptText("Nama menu");
        txtHarga.setPromptText("Harga");
        cmbTipe.getItems().addAll("Makanan", "Minuman");
        cmbTipe.setValue("Makanan");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.addRow(0, new Label("Nama:"), txtNama);
        grid.addRow(1, new Label("Harga:"), txtHarga);
        grid.addRow(2, new Label("Tipe:"), cmbTipe);

        // Buttons
        Button btnTambah = new Button("Tambah");
        Button btnUpdate = new Button("Update");
        Button btnHapus = new Button("Hapus");
        Button btnReset = new Button("Reset");

        btnTambah.setOnAction(e -> {
            if (selectedMenu != null) { UIHelper.showAlert(Alert.AlertType.WARNING, "Info", "Tekan Reset untuk tambah baru!"); return; }
            try {
                service.simpanMenu(null, txtNama.getText(), txtHarga.getText(), cmbTipe.getValue());
                refreshData(); resetForm();
            } catch (IllegalArgumentException ex) { UIHelper.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        });

        btnUpdate.setOnAction(e -> {
            if (selectedMenu == null) return;
            try {
                service.simpanMenu(selectedMenu.getId(), txtNama.getText(), txtHarga.getText(), cmbTipe.getValue());
                refreshData(); resetForm();
            } catch (IllegalArgumentException ex) { UIHelper.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        });

        btnHapus.setOnAction(e -> {
            if (selectedMenu == null) return;
            service.hapusMenu(selectedMenu.getId());
            refreshData(); resetForm();
        });

        btnReset.setOnAction(e -> resetForm());

        HBox buttons = new HBox(8, btnTambah, btnUpdate, btnHapus, btnReset);
        getChildren().addAll(lbl, tableMenu, new Label("Form Menu:"), grid, buttons);
    }

    public void refreshData() {
        tableMenu.getItems().setAll(service.ambilSemuaMenu());
    }

    private void resetForm() {
        txtNama.clear(); txtHarga.clear(); cmbTipe.setValue("Makanan");
        selectedMenu = null;
        tableMenu.getSelectionModel().clearSelection();
    }
}