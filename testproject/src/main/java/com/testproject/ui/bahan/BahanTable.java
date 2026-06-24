package com.testproject.ui.bahan;

import com.testproject.model.Bahan;
import com.testproject.service.BahanService;
import com.testproject.utils.UIHelper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BahanTable extends VBox {
    private final TableView<Bahan> table = new TableView<>();
    private final BahanService service;
    private final Runnable onDataChanged;

    private List<Bahan> allBahanData = new ArrayList<>();
    private final Pagination pagination = new Pagination();
    private final int ROWS_PER_PAGE = 15;

    public BahanTable(BahanService service, Consumer<Bahan> onRowSelected, Runnable onDataChanged) {
        this.service = service;
        this.onDataChanged = onDataChanged;
        
        setSpacing(10);
        VBox.setVgrow(pagination, Priority.ALWAYS);

        setupColumns();

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            onRowSelected.accept(newVal);
        });

        pagination.setPageFactory(this::createPage);

        HBox actionButtons = new HBox(10);
        Button btnRestock = new Button("Restock Bahan");
        btnRestock.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnRestock.setOnAction(e -> bukaDialogRestock());

        Button btnHapus = new Button("Hapus Bahan");
        btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnHapus.setOnAction(e -> hapusBahan());

        actionButtons.getChildren().addAll(btnRestock, btnHapus);
        getChildren().addAll(pagination, actionButtons);
    }

    private void setupColumns() {
        TableColumn<Bahan, String> colNama = new TableColumn<>("Nama Bahan");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNama.setPrefWidth(140);

        TableColumn<Bahan, String> colSatuan = new TableColumn<>("Satuan");
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colSatuan.setPrefWidth(65);

        // --- KOLOM BARU: HARGA BELI TERAKHIR ---
        TableColumn<Bahan, Double> colHarga = new TableColumn<>("Harga/Satuan");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaTerakhir"));
        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { 
                    setText(null); setStyle(""); 
                } else if (val == 0) { 
                    setText("Belum ada"); 
                    setStyle("-fx-text-fill: gray; -fx-font-style: italic;"); 
                } else { 
                    setText(String.format("Rp %,.0f", val).replace(",", ".")); 
                    setStyle(""); 
                }
            }
        });
        colHarga.setPrefWidth(100);
        // ---------------------------------------

        TableColumn<Bahan, Double> colJumlah = new TableColumn<>("Sisa Stok");
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colJumlah.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colJumlah.setPrefWidth(70);

        TableColumn<Bahan, Double> colStokMin = new TableColumn<>("Stok Min");
        colStokMin.setCellValueFactory(new PropertyValueFactory<>("stokMinimum"));
        colStokMin.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colStokMin.setPrefWidth(65);

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

        table.getColumns().addAll(colNama, colSatuan, colHarga, colJumlah, colStokMin, colStatus);
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allBahanData.size());
        
        if (fromIndex <= toIndex && !allBahanData.isEmpty()) {
            table.getItems().setAll(allBahanData.subList(fromIndex, toIndex));
        } else {
            table.getItems().clear();
        }
        
        return table; 
    }

    public void refreshData(List<Bahan> data) {
        this.allBahanData = data;
        
        int pageCount = (int) Math.ceil((double) allBahanData.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        
        int currPage = pagination.getCurrentPageIndex();
        if (currPage >= pageCount && pageCount > 0) {
            currPage = pageCount - 1;
        }
        
        pagination.setCurrentPageIndex(currPage);
        createPage(currPage); 
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