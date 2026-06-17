package com.testproject.ui.bahan;

import com.testproject.service.BahanService;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BahanPanel extends HBox {

    private final BahanService bahanService = new BahanService();
    private final BahanTable bahanTable;
    private final RiwayatPanel riwayatPanel;
    private final BahanForm bahanForm;

    public BahanPanel() {
        setSpacing(20);
        setPadding(new Insets(20));
        HBox.setHgrow(this, Priority.ALWAYS);

        // 1. Instansiasi Komponen
        riwayatPanel = new RiwayatPanel(bahanService);
        
        bahanForm = new BahanForm(bahanService, () -> {
            loadData(); // Callback: Refresh tabel jika tambah sukses
        });

        bahanTable = new BahanTable(bahanService,
                selectedBahan -> riwayatPanel.tampilkanRiwayat(selectedBahan), // Callback: Baris diklik
                () -> loadData() // Callback: Refresh tabel jika hapus/restock sukses
        );

        // 2. Susun Layout Kiri
        VBox leftPane = new VBox(15);
        leftPane.setPrefWidth(650);
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        Label title = new Label("Manajemen Bahan Baku");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        leftPane.getChildren().addAll(title, bahanForm, bahanTable);

        // 3. Gabungkan Kiri dan Kanan
        getChildren().addAll(leftPane, riwayatPanel);
        
        loadData(); // Muat data dari database
    }

    public void loadData() {
        bahanTable.refreshData(bahanService.ambilSemuaBahan());
        riwayatPanel.clearRiwayat();
    }
}