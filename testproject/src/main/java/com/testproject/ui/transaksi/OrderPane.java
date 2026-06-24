package com.testproject.ui.transaksi;

import com.testproject.model.DetailTransaksi;
import com.testproject.model.MenuItem;
import com.testproject.model.OpsiMenu;
import com.testproject.model.PilihanOpsi;
import com.testproject.model.StatusPembayaran;
import com.testproject.model.Transaksi;
import com.testproject.service.MenuService;
import com.testproject.service.TransaksiService;
import com.testproject.utils.PrinterHelper;
import com.testproject.utils.StoreConfig;
import com.testproject.utils.UIHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderPane extends HBox {
    private final TransaksiService transaksiService;
    private final MenuService menuService = new MenuService();
    private final Runnable onCheckoutSuccess;

    private final TextField txtSearchMenu = new TextField();
    private final TableView<MenuItem> tableMenu = new TableView<>();
    private final ObservableList<MenuItem> allMenuItems = FXCollections.observableArrayList();
    private final FilteredList<MenuItem> filteredMenu = new FilteredList<>(allMenuItems, p -> true);

    private final HBox categoryBox = new HBox(10);
    private final ToggleGroup categoryGroup = new ToggleGroup();
    private String selectedCategory = "Semua"; 

    private final TextField txtNamaPelanggan = new TextField();
    private final ComboBox<String> cmbTipePesanan = new ComboBox<>();
    private final TableView<ItemKeranjang> tableKeranjang = new TableView<>();
    private final ObservableList<ItemKeranjang> keranjang = FXCollections.observableArrayList();
    private final Label lblTotal = new Label("Total: Rp 0");
    private final ComboBox<String> cmbMetodeBayar = new ComboBox<>();
    private final ComboBox<String> cmbStatus = new ComboBox<>();

    public OrderPane(TransaksiService transaksiService, Runnable onCheckoutSuccess) {
        this.transaksiService = transaksiService;
        this.onCheckoutSuccess = onCheckoutSuccess;
        
        setSpacing(20);
        setPadding(new Insets(15));

        VBox leftPane = new VBox(10);
        HBox.setHgrow(leftPane, Priority.ALWAYS);

        txtSearchMenu.setPromptText("🔍 Cari menu berdasarkan nama...");
        txtSearchMenu.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");
        txtSearchMenu.textProperty().addListener((obs, old, val) -> applyFilters());

        categoryBox.setAlignment(Pos.CENTER_LEFT);

        Button btnManualItem = new Button("➕ Kasir Manual");
        btnManualItem.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px;");
        btnManualItem.setOnAction(e -> tampilkanDialogKasirManual());

        Button btnSettingStruk = new Button("⚙ Seting Struk");
        btnSettingStruk.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px;");
        btnSettingStruk.setOnAction(e -> tampilkanDialogSettingStruk());

        // --- FITUR BARU #4: KAS KELUAR ---
        Button btnKasKeluar = new Button("➖ Kas Keluar");
        btnKasKeluar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px;");
        btnKasKeluar.setOnAction(e -> tampilkanDialogKasKeluar());

        HBox searchContainer = new HBox(10, txtSearchMenu, btnManualItem, btnSettingStruk, btnKasKeluar);
        HBox.setHgrow(txtSearchMenu, Priority.ALWAYS);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        // ---------------------------------

        TableColumn<MenuItem, String> colNamaMenu = new TableColumn<>("Nama Menu");
        colNamaMenu.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNamaMenu.setPrefWidth(200);

        TableColumn<MenuItem, String> colTipeMenu = new TableColumn<>("Kategori");
        colTipeMenu.setCellValueFactory(new PropertyValueFactory<>("tipe"));
        colTipeMenu.setPrefWidth(100);

        TableColumn<MenuItem, Double> colHargaMenu = new TableColumn<>("Harga");
        colHargaMenu.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colHargaMenu.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colHargaMenu.setPrefWidth(100);

        TableColumn<MenuItem, Void> colAksiMenu = new TableColumn<>("Aksi");
        colAksiMenu.setCellFactory(col -> new TableCell<>() {
            private final Button btnAdd = new Button("✚ Tambah");
            {
                btnAdd.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
                btnAdd.setOnAction(e -> {
                    MenuItem menu = getTableView().getItems().get(getIndex());
                    prosesKlikMenu(menu);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnAdd);
                setAlignment(Pos.CENTER);
            }
        });
        colAksiMenu.setPrefWidth(100);

        tableMenu.getColumns().addAll(colNamaMenu, colTipeMenu, colHargaMenu, colAksiMenu);
        tableMenu.setItems(filteredMenu);
        VBox.setVgrow(tableMenu, Priority.ALWAYS);

        leftPane.getChildren().addAll(categoryBox, searchContainer, tableMenu);

        VBox rightPane = new VBox(12);
        rightPane.setPrefWidth(450); 
        rightPane.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-border-color: #dee2e6; -fx-border-radius: 8;");

        Label lblPesanan = new Label("📝 Detail Pesanan");
        lblPesanan.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        cmbTipePesanan.setItems(FXCollections.observableArrayList("Dine-in", "Take Away", "GoFood", "GrabFood"));
        cmbTipePesanan.setValue("Dine-in");
        cmbTipePesanan.setMaxWidth(Double.MAX_VALUE);
        txtNamaPelanggan.setPromptText("Nama pelanggan (opsional)");

        GridPane gridCustomer = new GridPane();
        gridCustomer.setHgap(10); gridCustomer.setVgap(8);
        gridCustomer.addRow(0, new Label("Tipe:"), cmbTipePesanan);
        gridCustomer.addRow(1, new Label("Nama:"), txtNamaPelanggan);

        TableColumn<ItemKeranjang, String> colNamaKeranjang = new TableColumn<>("Menu");
        colNamaKeranjang.setCellValueFactory(new PropertyValueFactory<>("namaMenu"));
        colNamaKeranjang.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    ItemKeranjang kItem = getTableRow().getItem();
                    if (kItem.getMenu().getTipe().equals("Kustom") && item.equals("Item Manual")) {
                        String ket = kItem.getKeteranganOpsi();
                        if (ket.contains(" (Rp")) {
                            setText("✏ " + ket.substring(0, ket.indexOf(" (Rp")).trim());
                        } else {
                            setText("✏ " + ket);
                        }
                    } else {
                        setText(item);
                    }
                }
            }
        });
        colNamaKeranjang.setPrefWidth(130);

        TableColumn<ItemKeranjang, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colQty.setPrefWidth(40);
        
        TableColumn<ItemKeranjang, String> colOpsi = new TableColumn<>("Opsi");
        colOpsi.setCellValueFactory(new PropertyValueFactory<>("keteranganOpsi"));
        colOpsi.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    ItemKeranjang kItem = getTableRow().getItem();
                    if (kItem.getMenu().getTipe().equals("Kustom")) {
                        setText("-"); 
                    } else {
                        setText(item);
                    }
                }
            }
        });
        colOpsi.setPrefWidth(120);

        TableColumn<ItemKeranjang, Double> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colSubtotal.setPrefWidth(90);

        tableKeranjang.getColumns().addAll(colNamaKeranjang, colQty, colOpsi, colSubtotal);
        tableKeranjang.setItems(keranjang);
        tableKeranjang.setPrefHeight(250);

        Button btnHapusItem = new Button("✖ Hapus Item Terpilih");
        btnHapusItem.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnHapusItem.setMaxWidth(Double.MAX_VALUE);
        btnHapusItem.setOnAction(e -> {
            ItemKeranjang selected = tableKeranjang.getSelectionModel().getSelectedItem();
            if (selected != null) { keranjang.remove(selected); updateTotal(); }
        });

        lblTotal.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        cmbMetodeBayar.setItems(FXCollections.observableArrayList("Cash", "QRIS", "Transfer"));
        cmbMetodeBayar.setValue("Cash"); cmbMetodeBayar.setMaxWidth(Double.MAX_VALUE);
        
        cmbStatus.setItems(FXCollections.observableArrayList("LUNAS", "SEDANG_DIPROSES", "REFUND", "BATAL"));
        cmbStatus.setValue("LUNAS"); cmbStatus.setMaxWidth(Double.MAX_VALUE);

        GridPane gridBayar = new GridPane(); gridBayar.setHgap(10); gridBayar.setVgap(8);
        gridBayar.addRow(0, new Label("Bayar via:"), cmbMetodeBayar);
        gridBayar.addRow(1, new Label("Status:"), cmbStatus);

        Button btnBayar = new Button("✔ PROSES PEMBAYARAN");
        btnBayar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12px;");
        btnBayar.setMaxWidth(Double.MAX_VALUE);
        btnBayar.setOnAction(e -> prosesBayar());

        Button btnBatal = new Button("✖ Kosongkan Keranjang");
        btnBatal.setMaxWidth(Double.MAX_VALUE);
        btnBatal.setOnAction(e -> resetOrder());

        rightPane.getChildren().addAll(
            lblPesanan, gridCustomer, new Separator(), 
            tableKeranjang, btnHapusItem, new Separator(),
            lblTotal, gridBayar, btnBayar, btnBatal
        );

        getChildren().addAll(leftPane, rightPane);
    }

    public void loadMenuData() { 
        allMenuItems.setAll(menuService.ambilSemuaMenu()); 
        refreshCategoryButtons(); 
    }

    private void refreshCategoryButtons() {
        categoryBox.getChildren().clear();
        categoryGroup.getToggles().clear();

        ToggleButton btnSemua = new ToggleButton("Semua");
        btnSemua.setToggleGroup(categoryGroup);
        btnSemua.setSelected(true);
        btnSemua.setOnAction(e -> { selectedCategory = "Semua"; applyFilters(); });
        categoryBox.getChildren().add(btnSemua);

        List<String> categories = allMenuItems.stream()
                .map(MenuItem::getTipe)
                .filter(tipe -> !tipe.equals("Kustom")) 
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        for (String cat : categories) {
            ToggleButton btnCat = new ToggleButton(cat);
            btnCat.setToggleGroup(categoryGroup);
            btnCat.setOnAction(e -> {
                if (btnCat.isSelected()) selectedCategory = cat;
                else { btnSemua.setSelected(true); selectedCategory = "Semua"; }
                applyFilters();
            });
            categoryBox.getChildren().add(btnCat);
        }
        applyFilters();
    }

    private void applyFilters() {
        String search = txtSearchMenu.getText().toLowerCase().trim();
        filteredMenu.setPredicate(item -> {
            if (item.getNama().equalsIgnoreCase("Item Manual") && item.getTipe().equals("Kustom")) return false;

            boolean matchesSearch = search.isEmpty() || item.getNama().toLowerCase().contains(search);
            boolean matchesCategory = selectedCategory.equals("Semua") || item.getTipe().equalsIgnoreCase(selectedCategory);
            return matchesSearch && matchesCategory;
        });
    }

    // --- LOGIKA JENDELA KAS KELUAR ---
    private void tampilkanDialogKasKeluar() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Catat Pengeluaran Kasir");

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        Label lblInfo = new Label("Gunakan fitur ini jika Anda mengambil uang dari laci kasir\n(misal: beli es batu, bayar parkir, dsb).");
        
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        
        TextField txtKet = new TextField(); txtKet.setPromptText("Contoh: Beli Es Batu");
        TextField txtNominal = new TextField(); txtNominal.setPromptText("Contoh: 15000");

        grid.addRow(0, new Label("Keterangan:"), txtKet);
        grid.addRow(1, new Label("Nominal (Rp):"), txtNominal);

        Button btnSimpan = new Button("✔ Potong Uang Laci");
        btnSimpan.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSimpan.setMaxWidth(Double.MAX_VALUE);
        
        btnSimpan.setOnAction(e -> {
            String ket = txtKet.getText().trim();
            if (ket.isEmpty()) {
                UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Keterangan tidak boleh kosong!"); return;
            }
            double nominal;
            try {
                nominal = Double.parseDouble(txtNominal.getText().trim());
                if (nominal <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Nominal harus angka positif!"); return;
            }

            // Trik Akuntansi: Buat transaksi dengan harga minus
            String tanggal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Transaksi t = new Transaksi(0, "[KAS KELUAR] " + ket, tanggal, -nominal, "Cash", StatusPembayaran.LUNAS, "Pengeluaran");
            
            boolean sukses = transaksiService.prosesPembayaran(t, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            if (sukses) {
                UIHelper.showAlert(Alert.AlertType.INFORMATION, "Sukses", "Pengeluaran berhasil dicatat. Total Kas di sistem telah dikurangi.");
                dialog.close();
                onCheckoutSuccess.run();
            } else {
                UIHelper.showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan ke database.");
            }
        });

        box.getChildren().addAll(lblInfo, grid, btnSimpan);
        dialog.setScene(new Scene(box, 350, -1));
        dialog.showAndWait();
    }
    // ---------------------------------

    private void tampilkanDialogSettingStruk() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Pengaturan Struk Kasir");

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        Label lblInfo = new Label("Ubah nama toko yang akan dicetak pada struk:");
        TextField txtNamaToko = new TextField(StoreConfig.getStoreName());
        
        Button btnSimpan = new Button("✔ Simpan");
        btnSimpan.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSimpan.setMaxWidth(Double.MAX_VALUE);
        
        btnSimpan.setOnAction(e -> {
            String namaBaru = txtNamaToko.getText().trim();
            if(namaBaru.isEmpty()) {
                UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama toko tidak boleh kosong!");
                return;
            }
            StoreConfig.setStoreName(namaBaru);
            UIHelper.showAlert(Alert.AlertType.INFORMATION, "Sukses", "Nama toko berhasil diubah! Struk selanjutnya akan menggunakan nama baru.");
            dialog.close();
        });

        box.getChildren().addAll(lblInfo, txtNamaToko, btnSimpan);
        dialog.setScene(new Scene(box, 350, -1));
        dialog.showAndWait();
    }

    private void tampilkanDialogKasirManual() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Input Item Manual");

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        
        Label lblInfo = new Label("Masukkan nama dan harga untuk produk di luar menu.");
        
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        
        TextField txtNama = new TextField(); txtNama.setPromptText("Contoh: Kerupuk Udang");
        TextField txtHarga = new TextField(); txtHarga.setPromptText("Contoh: 5000");
        Spinner<Integer> spnQty = new Spinner<>(1, 999, 1);
        spnQty.setPrefWidth(100);

        grid.addRow(0, new Label("Nama Item:"), txtNama);
        grid.addRow(1, new Label("Harga Satuan:"), txtHarga);
        grid.addRow(2, new Label("Jumlah:"), spnQty);

        Button btnSimpan = new Button("✚ Tambahkan ke Keranjang");
        btnSimpan.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSimpan.setMaxWidth(Double.MAX_VALUE);
        
        btnSimpan.setOnAction(e -> {
            String namaCustom = txtNama.getText().trim();
            if (namaCustom.isEmpty()) {
                UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama item tidak boleh kosong!"); return;
            }
            
            double hargaCustom;
            try {
                hargaCustom = Double.parseDouble(txtHarga.getText().trim());
                if (hargaCustom < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Harga harus berupa angka positif!"); return;
            }

            MenuItem baseManualMenu = allMenuItems.stream()
                .filter(m -> m.getNama().equalsIgnoreCase("Item Manual") && m.getTipe().equals("Kustom"))
                .findFirst().orElse(null);

            if (baseManualMenu == null) {
                menuService.simpanMenu(null, "Item Manual", "0", "Kustom");
                loadMenuData(); 
                baseManualMenu = allMenuItems.stream()
                    .filter(m -> m.getNama().equalsIgnoreCase("Item Manual") && m.getTipe().equals("Kustom"))
                    .findFirst().get();
            }

            int qty = spnQty.getValue();
            double subtotal = hargaCustom * qty;
            String keterangan = namaCustom + " (Rp " + String.format("%,.0f", hargaCustom) + ")";

            keranjang.add(new ItemKeranjang(baseManualMenu, qty, subtotal, keterangan, new ArrayList<>(), new ArrayList<>()));
            updateTotal();
            dialog.close();
        });

        box.getChildren().addAll(lblInfo, grid, btnSimpan);
        dialog.setScene(new Scene(box, 350, -1));
        dialog.showAndWait();
    }

    private void prosesKlikMenu(MenuItem menu) {
        List<OpsiMenu> opsiList = menuService.ambilOpsiByMenu(menu.getId());
        
        if (opsiList.isEmpty()) {
            keranjang.add(new ItemKeranjang(menu, 1, menu.getHarga(), "", new ArrayList<>(), new ArrayList<>()));
            updateTotal();
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Pilih Opsi: " + menu.getNama());

        VBox dialogVbox = new VBox(15);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setStyle("-fx-font-size: 14px;");

        Label lblHargaDasar = new Label(String.format("Harga Dasar: Rp %.0f", menu.getHarga()));
        lblHargaDasar.setStyle("-fx-font-weight: bold;");
        dialogVbox.getChildren().add(lblHargaDasar);

        Map<Integer, Object> inputOpsiMap = new HashMap<>();

        for (OpsiMenu opsi : opsiList) {
            if (opsi.getTipeOpsi().equals("checkbox")) {
                CheckBox chk = new CheckBox(opsi.getNamaOpsi() + (opsi.getHargaTambahan() > 0 ? String.format(" (+Rp %.0f)", opsi.getHargaTambahan()) : ""));
                inputOpsiMap.put(opsi.getId(), chk);
                dialogVbox.getChildren().add(chk);
            } else {
                List<PilihanOpsi> pilihanList = menuService.ambilPilihanByOpsi(opsi.getId());
                ComboBox<PilihanOpsi> cmb = new ComboBox<>();
                cmb.setItems(FXCollections.observableArrayList(pilihanList));
                cmb.setPromptText("Pilih " + opsi.getNamaOpsi() + (opsi.isWajib() ? " (Wajib)*" : ""));
                cmb.setMaxWidth(300);
                inputOpsiMap.put(opsi.getId(), cmb);
                dialogVbox.getChildren().addAll(new Label(opsi.getNamaOpsi() + ":"), cmb);
            }
        }

        HBox boxJumlah = new HBox(10);
        boxJumlah.setAlignment(Pos.CENTER_LEFT);
        Spinner<Integer> spnJumlah = new Spinner<>(1, 99, 1);
        spnJumlah.setPrefWidth(80);
        boxJumlah.getChildren().addAll(new Label("Jumlah Porsi:"), spnJumlah);
        dialogVbox.getChildren().addAll(new Separator(), boxJumlah);

        Button btnSimpanKeKeranjang = new Button("✚ Masukkan ke Keranjang");
        btnSimpanKeKeranjang.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px;");
        btnSimpanKeKeranjang.setMaxWidth(Double.MAX_VALUE);
        
        btnSimpanKeKeranjang.setOnAction(e -> {
            for (OpsiMenu opsi : opsiList) {
                if (opsi.isWajib() && opsi.getTipeOpsi().equals("pilihan")) {
                    ComboBox<?> cmb = (ComboBox<?>) inputOpsiMap.get(opsi.getId());
                    if (cmb != null && cmb.getValue() == null) { 
                        UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Opsi \"" + opsi.getNamaOpsi() + "\" wajib dipilih!"); 
                        return; 
                    }
                }
            }

            double totalHargaOpsi = 0;
            StringBuilder ketOpsi = new StringBuilder();
            List<Integer> checkedIds = new ArrayList<>();
            List<Integer> chosenIds = new ArrayList<>();

            for (OpsiMenu opsi : opsiList) {
                if (opsi.getTipeOpsi().equals("checkbox")) {
                    CheckBox chk = (CheckBox) inputOpsiMap.get(opsi.getId());
                    if (chk != null && chk.isSelected()) {
                        totalHargaOpsi += opsi.getHargaTambahan();
                        checkedIds.add(opsi.getId());
                        if (ketOpsi.length() > 0) ketOpsi.append(", "); ketOpsi.append(opsi.getNamaOpsi());
                    }
                } else {
                    ComboBox<?> cmb = (ComboBox<?>) inputOpsiMap.get(opsi.getId());
                    if (cmb != null && cmb.getValue() instanceof PilihanOpsi p) {
                        totalHargaOpsi += p.getHargaTambahan();
                        chosenIds.add(p.getId());
                        if (ketOpsi.length() > 0) ketOpsi.append(", "); ketOpsi.append(p.getLabel());
                    }
                }
            }

            int qty = spnJumlah.getValue();
            double subtotal = (menu.getHarga() + totalHargaOpsi) * qty;

            keranjang.add(new ItemKeranjang(menu, qty, subtotal, ketOpsi.toString(), checkedIds, chosenIds));
            updateTotal();
            dialog.close();
        });

        dialogVbox.getChildren().add(btnSimpanKeKeranjang);
        
        Scene scene = new Scene(dialogVbox, 400, -1);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void updateTotal() {
        double total = keranjang.stream().mapToDouble(ItemKeranjang::getSubtotal).sum();
        lblTotal.setText(String.format("Total: Rp %,.0f", total).replace(",", "."));
    }

    private void prosesBayar() {
        if (keranjang.isEmpty()) { UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Keranjang kosong!"); return; }
        
        String tanggal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        double total = keranjang.stream().mapToDouble(ItemKeranjang::getSubtotal).sum();
        String metode = cmbMetodeBayar.getValue();
        String nama = txtNamaPelanggan.getText().trim();
        String tipeOrder = cmbTipePesanan.getValue(); 

        Transaksi t = new Transaksi(0, nama.isEmpty() ? "-" : nama, tanggal, total, metode, StatusPembayaran.valueOf(cmbStatus.getValue()), tipeOrder);
        
        List<DetailTransaksi> details = new ArrayList<>();
        List<List<Integer>> checkedOpsiIdsList = new ArrayList<>();
        List<List<Integer>> chosenPilihanIdsList = new ArrayList<>();

        for (ItemKeranjang item : keranjang) {
            details.add(new DetailTransaksi(0, 0, item.getMenu().getId(), item.getJumlah(), item.getSubtotal(), item.getKeteranganOpsi()));
            checkedOpsiIdsList.add(item.getCheckedOpsiIds());
            chosenPilihanIdsList.add(item.getChosenPilihanIds());
        }

        boolean sukses = transaksiService.prosesPembayaran(t, details, checkedOpsiIdsList, chosenPilihanIdsList);
        if (!sukses) { UIHelper.showAlert(Alert.AlertType.ERROR, "Error", "Transaksi gagal! Cek stok bahan Anda."); return; }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Transaksi Berhasil");
        alert.setHeaderText("Transaksi sukses disimpan ke database.");
        alert.setContentText("Apakah Anda ingin mencetak struk untuk pesanan ini?");
        
        ButtonType btnCetak = new ButtonType("🖨 Cetak Struk");
        ButtonType btnTutup = new ButtonType("✖ Tidak");
        alert.getButtonTypes().setAll(btnCetak, btnTutup);
        
        alert.showAndWait().ifPresent(type -> {
            if (type == btnCetak) {
                PrinterHelper.cetakStruk(t, new ArrayList<>(keranjang));
            }
            resetOrder(); 
            onCheckoutSuccess.run();
        });
    }

    private void resetOrder() {
        keranjang.clear(); 
        txtNamaPelanggan.clear(); 
        cmbTipePesanan.setValue("Dine-in");
        txtSearchMenu.clear();
        updateTotal();
    }
}