package com.testproject.ui;

import com.testproject.dao.ShiftDao;
import com.testproject.model.ShiftKas;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class ShiftPanel extends VBox {

    private final ShiftDao shiftDao = new ShiftDao();
    private final ObservableList<ShiftKas> dataShift = FXCollections.observableArrayList();
    private final TableView<ShiftKas> tableShift = new TableView<>();

    private final TextField txtModalAwal = new TextField();
    private final Label lblStatusShift = new Label();
    private final Label lblModalAwal = new Label();
    private final Label lblPenjualan = new Label();
    private final Label lblTotalKas = new Label();

    private Button btnBuka;
    private Button btnTutup;

    private ShiftKas shiftAktif = null;

    public ShiftPanel() {
        setPadding(new Insets(20));
        setSpacing(15);

        Label title = new Label("Manajemen Shift & Kas");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox mainLayout = new HBox(20);
        mainLayout.getChildren().addAll(buildStatusPane(), buildHistoryPane());

        getChildren().addAll(title, mainLayout);
        loadData();
    }

    private VBox buildStatusPane() {
        // Status card
        lblStatusShift.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        lblModalAwal.setStyle("-fx-font-size: 13px;");
        lblPenjualan.setStyle("-fx-font-size: 13px;");
        lblTotalKas.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        VBox statusCard = new VBox(8,
            lblStatusShift,
            new Separator(),
            lblModalAwal,
            lblPenjualan,
            new Separator(),
            lblTotalKas
        );
        statusCard.setPadding(new Insets(15));
        statusCard.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #ddd; " +
                            "-fx-border-radius: 6; -fx-background-radius: 6;");
        statusCard.setPrefWidth(260);

        // Open shift form
        txtModalAwal.setPromptText("Contoh: 500000");
        txtModalAwal.setMaxWidth(Double.MAX_VALUE);

        Label lblForm = new Label("Modal Awal (Rp):");
        lblForm.setStyle("-fx-font-weight: bold;");

        btnBuka = new Button("🟢 Buka Shift");
        btnBuka.setMaxWidth(Double.MAX_VALUE);
        btnBuka.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        btnBuka.setOnAction(e -> bukaShift());

        btnTutup = new Button("🔴 Tutup Shift");
        btnTutup.setMaxWidth(Double.MAX_VALUE);
        btnTutup.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        btnTutup.setOnAction(e -> tutupShift());

        VBox pane = new VBox(12,
            new Label("Status Shift Saat Ini:"),
            statusCard,
            new Separator(),
            lblForm,
            txtModalAwal,
            btnBuka,
            btnTutup
        );
        pane.setPrefWidth(280);
        return pane;
    }

    private VBox buildHistoryPane() {
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
                if (empty || val == null || val.isEmpty()) {
                    setText("(Aktif)");
                    setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } else {
                    setText(val);
                    setStyle("");
                }
            }
        });
        colTutup.setPrefWidth(130);

        TableColumn<ShiftKas, Double> colModal = new TableColumn<>("Modal");
        colModal.setCellValueFactory(new PropertyValueFactory<>("modalAwal"));
        colModal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %.0f", val));
            }
        });
        colModal.setPrefWidth(100);

        TableColumn<ShiftKas, Double> colPenjualan = new TableColumn<>("Penjualan");
        colPenjualan.setCellValueFactory(new PropertyValueFactory<>("totalPenjualan"));
        colPenjualan.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %.0f", val));
            }
        });
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

        Label lbl = new Label("Riwayat Shift");
        lbl.setStyle("-fx-font-weight: bold;");

        VBox pane = new VBox(8, lbl, tableShift);
        pane.setPrefWidth(640);
        return pane;
    }

    private void bukaShift() {
        if (shiftAktif != null) {
            showAlert("Sudah ada shift aktif! Tutup shift dulu sebelum membuka yang baru.");
            return;
        }
        double modal = 0;
        try {
            String txt = txtModalAwal.getText().trim();
            if (!txt.isEmpty()) modal = Double.parseDouble(txt);
        } catch (NumberFormatException e) {
            showAlert("Modal awal harus berupa angka!");
            return;
        }
        int id = shiftDao.bukaShift(modal);
        if (id == -1) {
            showAlert("Gagal membuka shift!");
            return;
        }
        txtModalAwal.clear();
        loadData();
    }

    private void tutupShift() {
        if (shiftAktif == null) {
            showAlert("Tidak ada shift aktif!");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Tutup shift sekarang? Total penjualan akan dihitung otomatis.",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                shiftDao.tutupShift(shiftAktif.getId());
                loadData();
            }
        });
    }

    public void loadData() {
        dataShift.setAll(shiftDao.getAll());
        shiftAktif = shiftDao.getShiftAktif();
        updateStatusCard();
    }

    private void updateStatusCard() {
        if (shiftAktif == null) {
            lblStatusShift.setText("🔴 Tidak Ada Shift Aktif");
            lblStatusShift.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #c00;");
            lblModalAwal.setText("Modal Awal: -");
            lblPenjualan.setText("Penjualan: -");
            lblTotalKas.setText("Total Kas: -");
            btnBuka.setDisable(false);
            btnTutup.setDisable(true);
        } else {
            // Refresh live penjualan from DB
            ShiftKas fresh = shiftDao.getById(shiftAktif.getId());
            // Calculate live penjualan (same as tutup logic but read-only)
            double livePenjualan = getLivePenjualan(shiftAktif.getTanggalBuka());
            double liveKas = shiftAktif.getModalAwal() + livePenjualan;

            lblStatusShift.setText("🟢 Shift Aktif — ID #" + shiftAktif.getId());
            lblStatusShift.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");
            lblModalAwal.setText(String.format("Modal Awal: Rp %.0f", shiftAktif.getModalAwal()));
            lblPenjualan.setText(String.format("Penjualan (LUNAS): Rp %.0f", livePenjualan));
            lblTotalKas.setText(String.format("Estimasi Kas: Rp %.0f", liveKas));
            btnBuka.setDisable(true);
            btnTutup.setDisable(false);
        }
    }

    private double getLivePenjualan(String sejak) {
        try (var conn = com.testproject.db.DatabaseHelper.getConnection();
             var stmt = conn.prepareStatement(
                 "SELECT COALESCE(SUM(total_harga), 0) FROM transaksi " +
                 "WHERE status_pembayaran = 'LUNAS' AND tanggal >= ?")) {
            stmt.setString(1, sejak);
            var rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            System.out.println("Error getLivePenjualan: " + e.getMessage());
        }
        return 0;
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }
}