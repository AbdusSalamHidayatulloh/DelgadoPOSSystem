package com.testproject.ui.transaksi;

import com.testproject.model.StatusPembayaran;
import com.testproject.model.Transaksi;
import com.testproject.service.TransaksiService;
import com.testproject.utils.PrinterHelper;
import com.testproject.utils.UIHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class RiwayatTransaksiPane extends VBox {
    private final TransaksiService service;
    private final Runnable onDataChanged;
    
    private final TableView<Transaksi> tableTransaksi = new TableView<>();
    private final ObservableList<Transaksi> dataTransaksi = FXCollections.observableArrayList();
    
    private List<Transaksi> allDataTransaksi = new ArrayList<>(); 
    private final Pagination pagination = new Pagination();
    private final int ROWS_PER_PAGE = 15; 

    public RiwayatTransaksiPane(TransaksiService service, Runnable onDataChanged) {
        this.service = service;
        this.onDataChanged = onDataChanged;
        setSpacing(8);

        Label lbl = new Label("Riwayat Transaksi (Klik Ganda/Double Click untuk detail)");
        lbl.setStyle("-fx-font-weight: bold;");

        TableColumn<Transaksi, Integer> colId = new TableColumn<>("ID"); colId.setCellValueFactory(new PropertyValueFactory<>("id")); colId.setPrefWidth(40);
        TableColumn<Transaksi, String> colTanggal = new TableColumn<>("Tanggal"); colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal")); colTanggal.setPrefWidth(125);
        TableColumn<Transaksi, String> colNama = new TableColumn<>("Pelanggan"); colNama.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan")); colNama.setPrefWidth(100);
        TableColumn<Transaksi, String> colTipe = new TableColumn<>("Tipe"); colTipe.setCellValueFactory(new PropertyValueFactory<>("tipePesanan")); colTipe.setPrefWidth(85);
        TableColumn<Transaksi, Double> colTotal = new TableColumn<>("Total"); colTotal.setCellValueFactory(new PropertyValueFactory<>("totalHarga")); colTotal.setCellFactory(col -> new UIHelper.FormatDesimalCell<>()); colTotal.setPrefWidth(80);
        TableColumn<Transaksi, String> colMetode = new TableColumn<>("Metode"); colMetode.setCellValueFactory(new PropertyValueFactory<>("metodeBayar")); colMetode.setPrefWidth(65);

        TableColumn<Transaksi, StatusPembayaran> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusPembayaran"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(StatusPembayaran val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); } 
                else {
                    setText(val.toString());
                    switch (val) {
                        case LUNAS -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        case REFUND -> setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        case BATAL -> setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        case SEDANG_DIPROSES -> setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                    }
                }
            }
        });
        colStatus.setPrefWidth(110);

        tableTransaksi.getColumns().addAll(colId, colTanggal, colNama, colTipe, colTotal, colMetode, colStatus);
        tableTransaksi.setItems(dataTransaksi);
        tableTransaksi.setPrefHeight(450); 

        // --- FITUR BARU: MENDETEKSI DOUBLE CLICK ---
        tableTransaksi.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Jika diklik 2 kali
                Transaksi selected = tableTransaksi.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    tampilkanDetailTransaksi(selected);
                }
            }
        });
        // -------------------------------------------

        pagination.setPageFactory(this::createPage);

        ComboBox<StatusPembayaran> cmbStatusUpdate = new ComboBox<>(FXCollections.observableArrayList(StatusPembayaran.values()));
        cmbStatusUpdate.setPromptText("Pilih Status Baru");

        Button btnUpdateStatus = new Button("✎ Ubah Status");
        btnUpdateStatus.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnUpdateStatus.setOnAction(e -> {
            Transaksi selected = tableTransaksi.getSelectionModel().getSelectedItem();
            StatusPembayaran newStatus = cmbStatusUpdate.getValue();
            
            if (selected == null) { UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih transaksi dulu!"); return; }
            if (newStatus == null) { UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih status baru dari dropdown!"); return; }
            
            service.ubahStatus(selected.getId(), newStatus);
            onDataChanged.run();
        });

        Button btnHapus = new Button("✖ Hapus");
        btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnHapus.setOnAction(e -> {
            Transaksi selected = tableTransaksi.getSelectionModel().getSelectedItem();
            if (selected == null) { UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih transaksi dulu!"); return; }
            service.hapusTransaksi(selected.getId());
            onDataChanged.run();
        });

        // Tombol cetak struk lama di luar sengaja saya hapus agar UI lebih bersih, 
        // karena sekarang fitur cetaknya sudah dipindah ke dalam Pop-Up Detail.
        HBox actionBox = new HBox(10, cmbStatusUpdate, btnUpdateStatus, btnHapus);
        
        getChildren().addAll(lbl, pagination, actionBox);
    }

    // --- FUNGSI BARU: JENDELA DETAIL TRANSAKSI & CETAK STRUK ---
    private void tampilkanDetailTransaksi(Transaksi t) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Detail Transaksi #" + t.getId());

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        // 1. Header Informasi Transaksi
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15); infoGrid.setVgap(8);
        infoGrid.addRow(0, new Label("Tanggal:"), new Label(t.getTanggal()));
        infoGrid.addRow(1, new Label("Pelanggan:"), new Label(t.getNamaPelanggan()));
        infoGrid.addRow(2, new Label("Tipe Pesanan:"), new Label(t.getTipePesanan()));
        infoGrid.addRow(3, new Label("Metode Bayar:"), new Label(t.getMetodeBayar()));
        
        Label lblStatus = new Label(t.getStatusPembayaran().toString());
        lblStatus.setStyle("-fx-font-weight: bold;");
        infoGrid.addRow(4, new Label("Status:"), lblStatus);

        // 2. Tabel Daftar Menu yang Dipesan
        TableView<ItemKeranjang> tableDetail = new TableView<>();
        
        TableColumn<ItemKeranjang, String> colNama = new TableColumn<>("Nama Item");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaMenu"));
        colNama.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    ItemKeranjang kItem = getTableRow().getItem();
                    // Trik agar item manual tampil rapi seperti di keranjang kasir
                    if (kItem.getMenu().getTipe().equals("Kustom") && item.equals("Item Manual")) {
                        String ket = kItem.getKeteranganOpsi();
                        setText("✏ " + (ket.contains(" (Rp") ? ket.substring(0, ket.indexOf(" (Rp")).trim() : ket));
                    } else {
                        setText(item);
                    }
                }
            }
        });
        colNama.setPrefWidth(160);

        TableColumn<ItemKeranjang, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colQty.setPrefWidth(40);

        TableColumn<ItemKeranjang, String> colOpsi = new TableColumn<>("Keterangan / Opsi");
        colOpsi.setCellValueFactory(new PropertyValueFactory<>("keteranganOpsi"));
        colOpsi.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    ItemKeranjang kItem = getTableRow().getItem();
                    setText(kItem.getMenu().getTipe().equals("Kustom") ? "-" : item);
                }
            }
        });
        colOpsi.setPrefWidth(150);

        TableColumn<ItemKeranjang, Double> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSub.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colSub.setPrefWidth(90);

        tableDetail.getColumns().addAll(colNama, colQty, colOpsi, colSub);
        
        // Memanggil data detail dari database
        List<ItemKeranjang> details = service.ambilDetailKeranjang(t.getId());
        tableDetail.getItems().setAll(details);
        tableDetail.setPrefHeight(200);

        // 3. Footer Total Harga & Tombol Cetak
        Label lblTotal = new Label(String.format("Total: Rp %,.0f", t.getTotalHarga()).replace(",", "."));
        lblTotal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnCetak = new Button("🖨 Cetak Struk");
        btnCetak.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px;");
        btnCetak.setMaxWidth(Double.MAX_VALUE);
        btnCetak.setOnAction(e -> {
            PrinterHelper.cetakStruk(t, details); // Panggil printer
        });

        box.getChildren().addAll(infoGrid, new Separator(), new Label("Rincian Belanja:"), tableDetail, lblTotal, btnCetak);
        
        Scene scene = new Scene(box, 480, 520);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    // -------------------------------------------------------------

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allDataTransaksi.size());
        
        if (fromIndex <= toIndex && !allDataTransaksi.isEmpty()) {
            dataTransaksi.setAll(allDataTransaksi.subList(fromIndex, toIndex));
        } else {
            dataTransaksi.clear();
        }
        return tableTransaksi;
    }

    public void refreshData() {
        allDataTransaksi = service.ambilSemuaTransaksi();
        
        int pageCount = (int) Math.ceil((double) allDataTransaksi.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        
        int currPage = pagination.getCurrentPageIndex();
        if (currPage >= pageCount && pageCount > 0) currPage = pageCount - 1;
        
        pagination.setCurrentPageIndex(currPage);
        createPage(currPage); 
    }
}