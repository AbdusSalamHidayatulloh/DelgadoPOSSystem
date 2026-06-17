package com.testproject.ui.dashboard;

import com.testproject.model.Bahan;
import com.testproject.service.DashboardService;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;

import java.util.Map;

public class DashboardPanel extends VBox {

    private final DashboardService service = new DashboardService();
    private final PieChart pieChart = new PieChart();
    private final CategoryAxis xAxis = new CategoryAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
    private final ListView<String> listBahanKritis = new ListView<>();

    public DashboardPanel() {
        setPadding(new Insets(20));
        setSpacing(20);

        Label title = new Label("📊 Dashboard Analitik");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Konfigurasi Chart
        pieChart.setTitle("5 Menu Terlaris");
        pieChart.setPrefHeight(300);

        lineChart.setTitle("Pendapatan 7 Hari Terakhir");
        lineChart.setPrefHeight(300);
        lineChart.setLegendVisible(false);

        HBox chartBox = new HBox(20, pieChart, lineChart);
        HBox.setHgrow(lineChart, Priority.ALWAYS);

        // Panel Peringatan
        VBox warningBox = new VBox(10);
        Label lblWarning = new Label("⚠ Peringatan: Stok Bahan Menipis!");
        lblWarning.setStyle("-fx-font-weight: bold; -fx-text-fill: #d35400;");
        listBahanKritis.setPrefHeight(150);
        warningBox.getChildren().addAll(lblWarning, listBahanKritis);
        warningBox.setStyle("-fx-background-color: #fff3e0; -fx-padding: 10; -fx-border-color: #f39c12; -fx-border-radius: 5;");

        getChildren().addAll(title, chartBox, warningBox);
        loadData();
    }

    public void loadData() {
        // Load Pie Chart
        pieChart.getData().clear();
        for (Map.Entry<String, Integer> entry : service.getTop5MenuLaris().entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        // Load Line Chart
        lineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> entry : service.getPendapatan7HariTerakhir().entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        lineChart.getData().add(series);

        // Load Warning List
        listBahanKritis.getItems().clear();
        for (Bahan b : service.getBahanStokKritis()) {
            listBahanKritis.getItems().add(b.getNama() + " — Sisa: " + b.getJumlah() + " " + b.getSatuan() + " (Min: " + b.getStokMinimum() + ")");
        }
        if (listBahanKritis.getItems().isEmpty()) {
            listBahanKritis.getItems().add("Semua stok bahan aman! 🎉");
        }
    }
}