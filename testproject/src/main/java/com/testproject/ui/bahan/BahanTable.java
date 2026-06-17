package com.testproject.ui.bahan;

import com.testproject.model.Bahan;
import com.testproject.service.BahanService;
import com.testproject.utils.UIHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

public class BahanTable extends VBox {
    private final TableView<Bahan> table = new TableView<>();
    private final BahanService service;
    private final Runnable onDataChanged;

    public BahanTable(BahanService service, Consumer<Bahan> onRowSelected, Runnable onDataChanged) {
        this.service = service;
        this.onDataChanged = onDataChanged;
        
        setSpacing(10);
        VBox.setVgrow(table, Priority.ALWAYS);

        setupColumns();

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            onRowSelected.accept(newVal);
        });

        HBox actionButtons = new HBox(10);
        Button btnRestock = new Button("Restock Bahan");
        btnRestock.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnRestock.setOnAction(e -> bukaDialogRestock());

        Button btnHapus = new Button("Hapus Bahan");
        btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnHapus.setOnAction(e -> hapusBahan());

        actionButtons.getChildren().addAll(btnRestock, btnHapus);
        getChildren().addAll(table, actionButtons);
    }

    private void setupColumns() {
        TableColumn<Bahan, String> colNama = new TableColumn<>("Nama Bahan");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNama.setPrefWidth(150);

        TableColumn<Bahan, String> colSatuan = new TableColumn<>("Satuan");
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colSatuan.setPrefWidth(70);

        TableColumn<Bahan, Double> colJumlah = new TableColumn<>("Jumlah");
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colJumlah.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colJumlah.setPrefWidth(70);

        TableColumn<Bahan, Double> colStokMin = new TableColumn<>("Stok Min");
        colStokMin.setCellValueFactory(new PropertyValueFactory<>("stokMinimum"));
        colStokMin.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colStokMin.setPrefWidth(70);

        TableColumn<Bahan, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("nama")); 
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setText(null); setStyle(""); return; }
                Bahan b = getTableRow().getItem();
                if (b.isStokHabis()) { setText("✖ Stok Habis"); setStyle("-fx-text-fill: #cc0000; -fx-font-weight: bold;"); } 
                else if (b.isStokRendah()) { setText("⚠ Stok Rendah"); setStyle("-fx-text-fill: #e65c00; -fx-font-weight: bold;"); } 
                else { setText("✓ Aman"); setStyle("-fx-text-fill: green; -fx-font-weight: bold;"); }
            }
        });
        colStatus.setPrefWidth(110);

        table.getColumns().addAll(colNama, colSatuan, colJumlah, colStokMin, colStatus);
    }

    public void refreshData(List<Bahan> data) {
        table.getItems().setAll(data);
    }

    private void bukaDialogRestock() {
        Bahan selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih bahan baku pada tabel terlebih dahulu!");
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Restock Bahan: " + selected.getNama());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20)); grid.setHgap(10); grid.setVgap(10);

        grid.add(new Label("Jumlah Tambahan (" + selected.getSatuan() + "):"), 0, 0);
        TextField txtJumlah = new TextField(); grid.add(txtJumlah, 1, 0);

        grid.add(new Label("Total Biaya Pengeluaran (Rp):"), 0, 1);
        TextField txtBiaya = new TextField(); grid.add(txtBiaya, 1, 1);

        Button btnSimpan = new Button("Simpan Restock");
        btnSimpan.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        grid.add(btnSimpan, 1, 2);

        btnSimpan.setOnAction(e -> {
            try {
                if (service.prosesRestock(selected, txtJumlah.getText(), txtBiaya.getText(), "")) {
                    UIHelper.showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data restock berhasil disimpan.");
                    dialog.close();
                    onDataChanged.run(); 
                }
            } catch (IllegalArgumentException ex) {
                UIHelper.showAlert(Alert.AlertType.WARNING, "Validasi Gagal", ex.getMessage());
            }
        });

        dialog.setScene(new Scene(grid));
        dialog.showAndWait();
    }

    private void hapusBahan() {
        Bahan selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih bahan baku yang ingin dihapus."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hapus bahan '" + selected.getNama() + "'?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (service.hapusBahan(selected.getId())) {
                    UIHelper.showAlert(Alert.AlertType.INFORMATION, "Sukses", "Bahan baku berhasil dihapus.");
                    onDataChanged.run();
                }
            }
        });
    }
}   