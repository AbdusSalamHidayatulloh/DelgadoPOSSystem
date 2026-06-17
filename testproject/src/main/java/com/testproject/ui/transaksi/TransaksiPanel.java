package com.testproject.ui.transaksi;

import com.testproject.service.TransaksiService;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TransaksiPanel extends VBox {
    private final TransaksiService transaksiService = new TransaksiService();
    private final OrderPane orderPane;
    private final RiwayatTransaksiPane riwayatPane;

    public TransaksiPanel() {
        setPadding(new Insets(20));
        setSpacing(10);
        Label title = new Label("Transaksi & Kasir");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        riwayatPane = new RiwayatTransaksiPane(transaksiService, () -> loadData());
        orderPane = new OrderPane(transaksiService, () -> riwayatPane.refreshData());

        HBox mainLayout = new HBox(15);
        orderPane.setPrefWidth(420);
        riwayatPane.setPrefWidth(500);
        mainLayout.getChildren().addAll(orderPane, riwayatPane);

        getChildren().addAll(title, mainLayout);
        loadData();
    }

    public void loadData() {
        orderPane.loadMenuData();
        riwayatPane.refreshData();
    }
}