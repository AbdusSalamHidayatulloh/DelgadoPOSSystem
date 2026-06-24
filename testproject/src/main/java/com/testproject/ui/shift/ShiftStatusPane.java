package com.testproject.ui.shift;

import com.testproject.model.ShiftKas;
import com.testproject.model.StatusPembayaran;
import com.testproject.model.Transaksi;
import com.testproject.service.ShiftService;
import com.testproject.service.TransaksiService;
import com.testproject.utils.UIHelper;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ShiftStatusPane extends VBox {

    private final ShiftService service;
    private final TransaksiService transaksiService = new TransaksiService();
    private final Runnable onDataChanged;

    private final TextField txtModalAwal = new TextField();
    private final Label lblStatusShift = new Label();
    private final Label lblModalAwal = new Label();
    private final Label lblPenjualan = new Label();
    private final Label lblTotalKas = new Label();
    private final Button btnBuka = new Button("🟢 Buka Shift");
    private final Button btnTutup = new Button("🔴 Tutup Shift");
    
    // Tombol Baru
    private final Button btnTambahKas = new Button("➕ Tambah Kas");
    private final Button btnKasKeluar = new Button("➖ Kas Keluar");

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

        // Konfigurasi Tombol Kas
        btnTambahKas.setMaxWidth(Double.MAX_VALUE);
        btnTambahKas.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
        btnTambahKas.setOnAction(e -> tampilkanDialogKas(true));

        btnKasKeluar.setMaxWidth(Double.MAX_VALUE);
        btnKasKeluar.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        btnKasKeluar.setOnAction(e -> tampilkanDialogKas(false));

        getChildren().addAll(
            new Label("Status Shift Saat Ini:"), statusCard, 
            btnTambahKas, btnKasKeluar, 
            new Separator(), lblForm, txtModalAwal, btnBuka, btnTutup
        );
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
            btnTambahKas.setDisable(true);
            btnKasKeluar.setDisable(true);
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
            btnTambahKas.setDisable(false);
            btnKasKeluar.setDisable(false);
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

    private void tampilkanDialogKas(boolean isPemasukan) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(isPemasukan ? "Tambah Kas Masuk" : "Catat Kas Keluar");

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        String info = isPemasukan ? "Masukkan nominal uang tambahan ke laci kasir." : "Masukkan nominal uang yang diambil dari laci kasir.";
        Label lblInfo = new Label(info);
        
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        
        TextField txtKet = new TextField(); 
        txtKet.setPromptText(isPemasukan ? "Contoh: Modal tambahan" : "Contoh: Beli Es Batu");
        TextField txtNominal = new TextField(); 
        txtNominal.setPromptText("Contoh: 50000");

        grid.addRow(0, new Label("Keterangan:"), txtKet);
        grid.addRow(1, new Label("Nominal (Rp):"), txtNominal);

        Button btnSimpan = new Button(isPemasukan ? "✔ Tambah Kas" : "✔ Potong Kas");
        btnSimpan.setStyle(isPemasukan ? "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;" : "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSimpan.setMaxWidth(Double.MAX_VALUE);
        
        btnSimpan.setOnAction(e -> {
            String ket = txtKet.getText().trim();
            if (ket.isEmpty()) {
                UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Keterangan tidak boleh kosong!"); return;
            }
            double nominal;
            try {
                nominal = Double.parseDouble(txtNominal.getText().trim());
                if (nominal <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Nominal harus angka positif!"); return;
            }

            String tanggal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String tipe = isPemasukan ? "Pemasukan" : "Pengeluaran";
            String namaKet = isPemasukan ? "[KAS MASUK] " + ket : "[KAS KELUAR] " + ket;
            double hargaTransaksi = isPemasukan ? nominal : -nominal;

            Transaksi t = new Transaksi(0, namaKet, tanggal, hargaTransaksi, "Cash", StatusPembayaran.LUNAS, tipe);
            
            boolean sukses = transaksiService.prosesPembayaran(t, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            if (sukses) {
                UIHelper.showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data kas berhasil disimpan.");
                dialog.close();
                onDataChanged.run();
            } else {
                UIHelper.showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan ke database.");
            }
        });

        box.getChildren().addAll(lblInfo, grid, btnSimpan);
        dialog.setScene(new Scene(box, 350, -1));
        dialog.showAndWait();
    }
}