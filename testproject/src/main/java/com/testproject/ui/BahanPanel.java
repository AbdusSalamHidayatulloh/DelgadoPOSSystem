package com.testproject.ui;

import com.testproject.dao.BahanDao;
import com.testproject.model.Bahan;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;

public class BahanPanel extends VBox {

    private final BahanDao bahanDao = new BahanDao();
    private final TableView<Bahan> table = new TableView<>();
    private final ObservableList<Bahan> data = FXCollections.observableArrayList();

    // Form tambah bahan
    private final TextField txtNama = new TextField();
    private final TextField txtSatuan = new TextField();
    private final TextField txtJumlah = new TextField();
    private final TextField txtStokMin = new TextField();

    // Form restock
    private final TextField txtRestockJumlah = new TextField();
    private final TextField txtRestockHarga = new TextField();

    private Bahan selectedBahan = null;

    // Panels
    private final VBox formTambah = new VBox(8);
    private final VBox formRestock = new VBox(8);

    public BahanPanel() {
        setPadding(new Insets(20));
        setSpacing(15);

        Label title = new Label("Manajemen Bahan");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        setupTable();
        buildFormTambah();
        buildFormRestock();

        // Default: tampilkan form tambah, sembunyikan restock
        formRestock.setVisible(false);
        formRestock.setManaged(false);

        getChildren().addAll(title, table, formTambah, formRestock);
        loadData();
    }

    private void setupTable() {
        TableColumn<Bahan, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNama.setPrefWidth(150);

        TableColumn<Bahan, String> colSatuan = new TableColumn<>("Satuan");
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colSatuan.setPrefWidth(80);

        TableColumn<Bahan, Double> colJumlah = new TableColumn<>("Jumlah");
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colJumlah.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    return;
                }
                // Show decimal only if needed
                setText(val == Math.floor(val) ? String.valueOf(val.intValue()) : String.valueOf(val));
            }
        });
        colJumlah.setPrefWidth(80);

        TableColumn<Bahan, Double> colStokMin = new TableColumn<>("Stok Minimum");
        colStokMin.setCellValueFactory(new PropertyValueFactory<>("stokMinimum"));
        colStokMin.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    return;
                }
                setText(val == Math.floor(val) ? String.valueOf(val.intValue()) : String.valueOf(val));
            }
        });
        colStokMin.setPrefWidth(110);

        // NEW: Satuan between Stok Minimum and Status
        TableColumn<Bahan, String> colSatuanMin = new TableColumn<>("Satuan");
        colSatuanMin.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colSatuanMin.setPrefWidth(70);

        TableColumn<Bahan, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                Bahan b = getTableRow().getItem();
                if (b.getJumlah() <= 0) {
                    setText("✖ Stok Habis");
                    setStyle("-fx-text-fill: #cc0000; -fx-font-weight: bold;");
                } else if (b.isStokRendah()) {
                    setText("⚠ Stok Rendah");
                    setStyle("-fx-text-fill: #e65c00;");
                } else {
                    setText("✓ Aman");
                    setStyle("-fx-text-fill: green;");
                }
            }
        });
        colStatus.setPrefWidth(120);

        table.getColumns().addAll(colNama, colSatuan, colJumlah, colStokMin, colSatuanMin, colStatus);
        table.setItems(data);
        table.setPrefHeight(250);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedBahan = newVal;
                txtNama.setText(newVal.getNama());
                txtSatuan.setText(newVal.getSatuan());
                txtJumlah.setText(formatDouble(newVal.getJumlah()));
                txtStokMin.setText(formatDouble(newVal.getStokMinimum()));
                showRestockForm(true);
            }
        });
    }

    private String formatDouble(double val) {
        return val == Math.floor(val) ? String.valueOf((int) val) : String.valueOf(val);
    }

    private void buildFormTambah() {
        txtNama.setPromptText("Nama bahan");
        txtSatuan.setPromptText("Satuan (kg, liter, pcs...)");
        txtJumlah.setPromptText("Jumlah stok awal");
        txtStokMin.setPromptText("Stok minimum");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Nama:"), txtNama, new Label("Satuan:"), txtSatuan);
        grid.addRow(1, new Label("Jumlah Awal:"), txtJumlah, new Label("Stok Min:"), txtStokMin);

        Button btnTambah = new Button("✚ Tambah Bahan");
        Button btnUpdate = new Button("✎ Update");
        Button btnHapus = new Button("✖ Hapus");
        Button btnReset = new Button("Reset");

        btnTambah.setOnAction(e -> {
            if (selectedBahan != null) {
                showAlert("Bahan sudah dipilih! Tekan Reset untuk tambah bahan baru.");
                return;
            }
            Bahan b = getBahanFromForm();
            if (b != null) {
                bahanDao.add(b);
                loadData();
                resetForm();
            }
        });

        btnUpdate.setOnAction(e -> {
            if (selectedBahan == null) {
                showAlert("Pilih bahan yang ingin diupdate!");
                return;
            }
            Bahan b = getBahanFromForm();
            if (b != null) {
                b.setId(selectedBahan.getId());
                bahanDao.update(b);
                loadData();
                resetForm();
            }
        });

        btnHapus.setOnAction(e -> {
            if (selectedBahan == null) {
                showAlert("Pilih bahan yang ingin dihapus!");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Yakin hapus bahan \"" + selectedBahan.getNama() + "\"?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    bahanDao.delete(selectedBahan.getId());
                    loadData();
                    resetForm();
                }
            });
        });

        btnReset.setOnAction(e -> resetForm());

        HBox buttons = new HBox(10, btnTambah, btnUpdate, btnHapus, btnReset);

        Label formLabel = new Label("Form Bahan:");
        formLabel.setStyle("-fx-font-weight: bold;");
        formTambah.getChildren().addAll(formLabel, grid, buttons);
    }

    private void buildFormRestock() {
        txtRestockJumlah.setPromptText("Jumlah yang didapat");
        txtRestockHarga.setPromptText("Harga total bayar");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Jumlah Didapat:"), txtRestockJumlah,
                new Label("Harga Total:"), txtRestockHarga);

        // Label preview harga per satuan
        Label lblPreview = new Label("Harga/satuan: -");
        lblPreview.setStyle("-fx-text-fill: #555;");

        // Auto kalkulasi preview saat user mengetik
        txtRestockJumlah.textProperty().addListener((obs, o, n) -> updatePreview(lblPreview));
        txtRestockHarga.textProperty().addListener((obs, o, n) -> updatePreview(lblPreview));

        Button btnRestock = new Button("✚ Restock");
        Button btnBatal = new Button("Batal");

        btnRestock.setOnAction(e -> {
            if (selectedBahan == null)
                return;
            try {
                double jumlah = Double.parseDouble(txtRestockJumlah.getText().trim());
                double hargaTotal = Double.parseDouble(txtRestockHarga.getText().trim());
                if (jumlah <= 0 || hargaTotal <= 0) {
                    showAlert("Jumlah dan harga harus lebih dari 0!");
                    return;
                }
                String tanggal = LocalDate.now().toString();
                bahanDao.restock(selectedBahan.getId(), jumlah, hargaTotal, tanggal);
                loadData();
                resetForm();
            } catch (NumberFormatException ex) {
                showAlert("Jumlah dan harga harus berupa angka!");
            }
        });

        btnBatal.setOnAction(e -> resetForm());

        HBox buttons = new HBox(10, btnRestock, btnBatal);

        Label restockLabel = new Label("Restock Bahan:");
        restockLabel.setStyle("-fx-font-weight: bold;");

        formRestock.getChildren().addAll(restockLabel, grid, lblPreview, buttons);
        formRestock.setStyle(
                "-fx-background-color: #f0f7ff; -fx-padding: 10; -fx-border-color: #aaccff; -fx-border-radius: 5;");
    }

    private void updatePreview(Label lbl) {
        try {
            double jumlah = Double.parseDouble(txtRestockJumlah.getText().trim());
            double hargaTotal = Double.parseDouble(txtRestockHarga.getText().trim());
            if (jumlah > 0) {
                double perSatuan = hargaTotal / jumlah;
                lbl.setText(String.format("Harga/satuan: Rp %.0f", perSatuan));
            }
        } catch (NumberFormatException e) {
            lbl.setText("Harga/satuan: -");
        }
    }

    private void showRestockForm(boolean show) {
        formRestock.setVisible(show);
        formRestock.setManaged(show);
        txtRestockJumlah.clear();
        txtRestockHarga.clear();
    }

    private Bahan getBahanFromForm() {
        try {
            String nama = txtNama.getText().trim();
            String satuan = txtSatuan.getText().trim();
            double jumlah = Double.parseDouble(txtJumlah.getText().trim());
            double stokMin = Double.parseDouble(txtStokMin.getText().trim());

            if (nama.isEmpty() || satuan.isEmpty()) {
                showAlert("Nama dan satuan tidak boleh kosong!");
                return null;
            }

            return new Bahan(0, nama, satuan, jumlah, stokMin);
        } catch (NumberFormatException e) {
            showAlert("Jumlah dan stok minimum harus berupa angka!");
            return null;
        }
    }

    public void loadData() {
        data.setAll(bahanDao.getAll());
    }

    private void resetForm() {
        txtNama.clear();
        txtSatuan.clear();
        txtJumlah.clear();
        txtStokMin.clear();
        selectedBahan = null;
        table.getSelectionModel().clearSelection();
        showRestockForm(false);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }
}