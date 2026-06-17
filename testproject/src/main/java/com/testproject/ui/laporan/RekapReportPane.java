package com.testproject.ui.laporan;

import com.testproject.service.LaporanService;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class RekapReportPane extends VBox {
    private final LaporanService service;
    private final DatePicker dpDari = new DatePicker(LocalDate.now().minusMonths(1));
    private final DatePicker dpSampai = new DatePicker(LocalDate.now());

    private final Label lblRekapPenghasilan = new Label("Total Penghasilan: Rp 0");
    private final Label lblRekapPengeluaran = new Label("Total Pengeluaran: Rp 0");
    private final Label lblRekapProfit = new Label("Estimasi Profit: Rp 0");

    public RekapReportPane(LaporanService service) {
        this.service = service;
        setPadding(new Insets(20));
        setSpacing(15);

        lblRekapPenghasilan.setStyle("-fx-font-size: 14px;");
        lblRekapPengeluaran.setStyle("-fx-font-size: 14px;");
        lblRekapProfit.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button btnFilter = new Button("🔍 Filter");
        btnFilter.setOnAction(e -> loadData());
        HBox filter = new HBox(10, new Label("Dari:"), dpDari, new Label("Sampai:"), dpSampai, btnFilter);
        filter.setPadding(new Insets(10, 0, 10, 0));

        getChildren().addAll(filter, new Separator(), lblRekapPenghasilan, lblRekapPengeluaran, new Separator(), lblRekapProfit);
    }

    public void loadData() {
        if (dpDari.getValue() != null && dpSampai.getValue() != null) {
            String dari = dpDari.getValue().toString();
            String sampai = dpSampai.getValue().toString();

            double penghasilan = service.getPenghasilan(dari, sampai);
            double pengeluaran = service.getPengeluaran(dari, sampai);
            double profit = penghasilan - pengeluaran;

            lblRekapPenghasilan.setText(String.format("Total Penghasilan: Rp %.0f", penghasilan));
            lblRekapPengeluaran.setText(String.format("Total Pengeluaran Bahan: Rp %.0f", pengeluaran));
            lblRekapProfit.setText(String.format("Estimasi Profit: Rp %.0f", profit));
            lblRekapProfit.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + (profit >= 0 ? "green" : "red") + ";");
        }
    }
}