package com.testproject.ui.menu;

import com.testproject.service.MenuService;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Instansiasi Komponen
        opsiTab = new MenuOpsiTab(menuService);
        resepTab = new MenuResepTab(menuService);

        leftPane = new MenuLeftPane(menuService, selectedMenu -> {
            // Callback: Beritahu Tab Kanan jika menu dipilih
            opsiTab.setMenu(selectedMenu);
            resepTab.setMenu(selectedMenu);
        });

        // Setup TabPane untuk bagian kanan
        TabPane rightTabs = new TabPane();
        rightTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        rightTabs.getTabs().addAll(
            new Tab("⚙ Opsi Menu", opsiTab),
            new Tab("📋 Resep & Bahan", resepTab)
        );
        rightTabs.setPrefWidth(550);
        HBox.setHgrow(rightTabs, Priority.ALWAYS);

        // Gabungkan kiri dan kanan
        HBox mainLayout = new HBox(20);
        mainLayout.getChildren().addAll(leftPane, rightTabs);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        getChildren().addAll(title, mainLayout);
        loadData();
    }

    public void loadData() {
        // Muat data menu
        leftPane.refreshData();
        // Berikan list bahan ke Tab Resep agar ComboBox-nya ter-update
        resepTab.refreshBahanList(menuService.ambilSemuaBahan());
    }
}