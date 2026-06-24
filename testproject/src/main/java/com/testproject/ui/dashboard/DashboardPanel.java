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
    private final VBox chartContainer = new VBox(20); // Container khusus grafik
    private final ListView<String> listBahanKritis = new ListView<>();

    public DashboardPanel() {
        setPadding(new Insets(20));
        setSpacing(20);

        Label title = new Label("📊 Dashboard Analitik");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // Panel Peringatan Stok
        VBox warningBox = new VBox(10);
        Label lblWarning = new Label("⚠ Peringatan: Stok Bahan Menipis!");
        lblWarning.setStyle("-fx-font-weight: bold; -fx-text-fill: #d35400;");
        listBahanKritis.setPrefHeight(150);
        warningBox.getChildren().addAll(lblWarning, listBahanKritis);
        warningBox.setStyle("-fx-background-color: #fff3e0; -fx-padding: 10; -fx-border-color: #f39c12; -fx-border-radius: 5;");

        getChildren().addAll(title, chartContainer, warningBox);
        loadData();
    }

    public void loadData() {
        chartContainer.getChildren().clear();

        PieChart pieChart = new PieChart();
        pieChart.setTitle("5 Menu Terlaris");
        pieChart.setPrefHeight(300);
        pieChart.setAnimated(false);
        for (Map.Entry<String, Integer> entry : service.getTop5MenuLaris().entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Pendapatan 7 Hari Terakhir");
        lineChart.setPrefHeight(300);
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> entry : service.getPendapatan7HariTerakhir().entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        lineChart.getData().add(series);

        HBox chartBox = new HBox(20, pieChart, lineChart);
        HBox.setHgrow(lineChart, Priority.ALWAYS);
        chartContainer.getChildren().add(chartBox);

        listBahanKritis.getItems().clear();
        for (Bahan b : service.getBahanStokKritis()) {
            listBahanKritis.getItems().add(b.getNama() + " — Sisa: " + b.getJumlah() + " " + b.getSatuan() + " (Min: " + b.getStokMinimum() + ")");
        }
        if (listBahanKritis.getItems().isEmpty()) {
            listBahanKritis.getItems().add("Semua stok bahan aman! 🎉");
        }
    }
}