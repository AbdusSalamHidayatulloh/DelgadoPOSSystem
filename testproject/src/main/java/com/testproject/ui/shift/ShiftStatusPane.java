package com.testproject.ui.shift;

import com.testproject.model.ShiftKas;
import com.testproject.service.ShiftService;
import com.testproject.utils.UIHelper;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ShiftStatusPane extends VBox {

    private final ShiftService service;
    private final Runnable onDataChanged;

    private final TextField txtModalAwal = new TextField();
    private final Label lblStatusShift = new Label();
    private final Label lblModalAwal = new Label();
    private final Label lblPenjualan = new Label();
    private final Label lblTotalKas = new Label();
    private final Button btnBuka = new Button("🟢 Buka Shift");
    private final Button btnTutup = new Button("🔴 Tutup Shift");

    private ShiftKas shiftAktif = null;

    public ShiftStatusPane(ShiftService service, Runnable onDataChanged) {
        this.service = service;
        this.onDataChanged = onDataChanged;
        
        setSpacing(12);
        setPrefWidth(280);

        lblStatusShift.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        lblModalAwal.setStyle("-fx-font-size: 13px;");
        lblPenjualan.setStyle("-fx-font-size: 13px;");
        lblTotalKas.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        VBox statusCard = new VBox(8, lblStatusShift, new Separator(), lblModalAwal, lblPenjualan, new Separator(), lblTotalKas);
        statusCard.setPadding(new Insets(15));
        statusCard.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #ddd; -fx-border-radius: 6; -fx-background-radius: 6;");

        txtModalAwal.setPromptText("Contoh: 500000");
        txtModalAwal.setMaxWidth(Double.MAX_VALUE);

        Label lblForm = new Label("Modal Awal (Rp):");
        lblForm.setStyle("-fx-font-weight: bold;");

        btnBuka.setMaxWidth(Double.MAX_VALUE);
        btnBuka.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        btnBuka.setOnAction(e -> bukaShift());

        btnTutup.setMaxWidth(Double.MAX_VALUE);
        btnTutup.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        btnTutup.setOnAction(e -> tutupShift());

        getChildren().addAll(new Label("Status Shift Saat Ini:"), statusCard, new Separator(), lblForm, txtModalAwal, btnBuka, btnTutup);
    }

    public void updateStatusCard(ShiftKas aktif) {
        this.shiftAktif = aktif;
        if (shiftAktif == null) {
            lblStatusShift.setText("🔴 Tidak Ada Shift Aktif");
            lblStatusShift.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #c00;");
            lblModalAwal.setText("Modal Awal: -");
            lblPenjualan.setText("Penjualan: -");
            lblTotalKas.setText("Total Kas: -");
            btnBuka.setDisable(false);
            btnTutup.setDisable(true);
        } else {
            double livePenjualan = service.hitungLivePenjualan(shiftAktif.getTanggalBuka());
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

    private void bukaShift() {
        if (shiftAktif != null) {
            UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Sudah ada shift aktif! Tutup shift dulu sebelum membuka yang baru.");
            return;
        }
        try {
            int id = service.bukaShiftBaru(txtModalAwal.getText());
            if (id != -1) {
                txtModalAwal.clear();
                onDataChanged.run();
            } else {
                UIHelper.showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal membuka shift di database!");
            }
        } catch (IllegalArgumentException ex) {
            UIHelper.showAlert(Alert.AlertType.WARNING, "Validasi", ex.getMessage());
        }
    }

    private void tutupShift() {
        if (shiftAktif == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Tutup shift sekarang? Total penjualan akan dihitung otomatis.", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                service.tutupShift(shiftAktif.getId());
                onDataChanged.run();
            }
        });
    }
}