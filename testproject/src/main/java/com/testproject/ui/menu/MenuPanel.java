package com.testproject.ui.menu;

import com.testproject.service.MenuService;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MenuPanel extends VBox {

    private final MenuService menuService = new MenuService();
    private final MenuLeftPane leftPane;
    private final MenuOpsiTab opsiTab;
    private final MenuResepTab resepTab;

    public MenuPanel() {
        setPadding(new Insets(20));
        setSpacing(10);

        Label title = new Label("Manajemen Menu & Resep");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Instansiasi Komponen Kanan
        opsiTab = new MenuOpsiTab(menuService);
        resepTab = new MenuResepTab(menuService);

        // KANAN: Menyusun Opsi dan Resep secara vertikal (Atas-Bawah)
        SplitPane rightSplitPane = new SplitPane();
        rightSplitPane.setOrientation(Orientation.VERTICAL);
        rightSplitPane.getItems().addAll(opsiTab, resepTab);
        rightSplitPane.setDividerPositions(0.4); // 40% ruang atas untuk Opsi, 60% bawah untuk Resep

        // KANAN: UI Petunjuk saat kosong (Empty State)
        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        Label iconEmpty = new Label("👈");
        iconEmpty.setStyle("-fx-font-size: 40px;");
        Label textEmpty = new Label("Pilih menu di tabel sebelah kiri\nuntuk mulai mengatur Opsi dan Resep.");
        textEmpty.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
        textEmpty.setAlignment(Pos.CENTER);
        textEmpty.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        emptyState.getChildren().addAll(iconEmpty, textEmpty);

        // KANAN: StackPane untuk menimpa Empty State dengan SplitPane
        StackPane rightContainer = new StackPane();
        rightContainer.getChildren().addAll(emptyState, rightSplitPane);
        HBox.setHgrow(rightContainer, Priority.ALWAYS);
        
        // Default awal: Sembunyikan form kanan, tampilkan Empty State
        rightSplitPane.setVisible(false);

        // KIRI: Komponen Tabel Menu Utama
        leftPane = new MenuLeftPane(menuService, selectedMenu -> {
            if (selectedMenu == null) {
                // Jika tidak ada yang dipilih, kembalikan ke layar kosong
                rightSplitPane.setVisible(false);
                emptyState.setVisible(true);
            } else {
                // Jika diklik, sembunyikan layar kosong, tampilkan form
                emptyState.setVisible(false);
                rightSplitPane.setVisible(true);
                opsiTab.setMenu(selectedMenu);
                resepTab.setMenu(selectedMenu);
            }
        });

        // Gabungkan Kiri dan Kanan
        HBox mainLayout = new HBox(20);
        mainLayout.getChildren().addAll(leftPane, rightContainer);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        getChildren().addAll(title, mainLayout);
        loadData();
    }

    public void loadData() {
        leftPane.refreshData();
        resepTab.refreshBahanList(menuService.ambilSemuaBahan());
    }
}