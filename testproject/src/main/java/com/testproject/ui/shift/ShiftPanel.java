package com.testproject.ui.shift;

import com.testproject.model.ShiftKas;
import com.testproject.service.ShiftService;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ShiftPanel extends VBox {

    private final ShiftService shiftService = new ShiftService();
    private final ShiftStatusPane statusPane;
    private final ShiftHistoryPane historyPane;

    public ShiftPanel() {
        setPadding(new Insets(20));
        setSpacing(15);

        Label title = new Label("Manajemen Shift & Kas");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        historyPane = new ShiftHistoryPane();
        statusPane = new ShiftStatusPane(shiftService, () -> loadData());

        HBox mainLayout = new HBox(20);
        mainLayout.getChildren().addAll(statusPane, historyPane);

        getChildren().addAll(title, mainLayout);
        loadData();
    }

    public void loadData() {
        ShiftKas aktif = shiftService.ambilShiftAktif();
        statusPane.updateStatusCard(aktif);
        historyPane.refreshData(shiftService.ambilSemuaShift());
    }
}