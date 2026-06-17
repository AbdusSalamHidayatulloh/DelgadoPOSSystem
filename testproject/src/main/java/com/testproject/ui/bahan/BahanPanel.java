package com.testproject.ui.bahan;

import com.testproject.service.BahanService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class BahanPanel extends VBox {

    private final BahanService bahanService = new BahanService();
    private final BahanTable bahanTable;
    private final RiwayatPanel riwayatPanel;
    private final BahanForm bahanForm;

    public BahanPanel() {
        setPadding(new Insets(20));
        setSpacing(15);

        Label title = new Label("Manajemen Bahan Baku");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // --- KOMPONEN KANAN (Riwayat & Empty State) ---
        riwayatPanel = new RiwayatPanel(bahanService);
        
        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        Label iconEmpty = new Label("👈");
        iconEmpty.setStyle("-fx-font-size: 40px;");
        Label textEmpty = new Label("Pilih bahan baku di tabel sebelah kiri\nuntuk melihat Riwayat Restock.");
        textEmpty.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
        textEmpty.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        emptyState.getChildren().addAll(iconEmpty, textEmpty);

        StackPane rightContainer = new StackPane();
        rightContainer.getChildren().addAll(emptyState, riwayatPanel);
        riwayatPanel.setVisible(false); // Sembunyikan riwayat di awal

        // --- KOMPONEN KIRI (Form & Tabel) ---
        bahanForm = new BahanForm(bahanService, () -> loadData());
        // Bungkus form agar terlihat seperti kartu rapi
        VBox formCard = new VBox(bahanForm);
        formCard.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        
        bahanTable = new BahanTable(bahanService,
                selectedBahan -> {
                    if (selectedBahan == null) {
                        riwayatPanel.setVisible(false);
                        emptyState.setVisible(true);
                    } else {
                        emptyState.setVisible(false);
                        riwayatPanel.setVisible(true);
                        riwayatPanel.tampilkanRiwayat(selectedBahan);
                    }
                },
                () -> loadData()
        );

        VBox leftContainer = new VBox(15);
        leftContainer.getChildren().addAll(formCard, bahanTable);

        // --- GABUNGAN DENGAN SPLIT PANE ---
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftContainer, rightContainer);
        splitPane.setDividerPositions(0.65); // 65% Kiri, 35% Kanan
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        getChildren().addAll(title, splitPane);
        loadData();
    }

    public void loadData() {
        bahanTable.refreshData(bahanService.ambilSemuaBahan());
    }
}