package com.testproject.ui;

import com.testproject.dao.BahanDao;
import com.testproject.dao.MenuDao;
import com.testproject.dao.OpsiMenuDao;
import com.testproject.dao.TransaksiDao;
import com.testproject.model.DetailTransaksi;
import com.testproject.model.MenuItem;
import com.testproject.model.OpsiMenu;
import com.testproject.model.PilihanOpsi;
import com.testproject.model.StatusPembayaran;
import com.testproject.model.Transaksi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransaksiPanel extends VBox {

    private final MenuDao menuDao = new MenuDao();
    private final OpsiMenuDao opsiDao = new OpsiMenuDao();
    private final TransaksiDao transaksiDao = new TransaksiDao();
    private final BahanDao bahanDao = new BahanDao();

    private final TableView<ItemKeranjang> tableKeranjang = new TableView<>();
    private final ObservableList<ItemKeranjang> keranjang = FXCollections.observableArrayList();

    private final TableView<Transaksi> tableTransaksi = new TableView<>();
    private final ObservableList<Transaksi> dataTransaksi = FXCollections.observableArrayList();

    private final TextField txtNamaPelanggan = new TextField();
    private final VBox opsiContainer = new VBox(8);
    private final ComboBox<String> cmbMetodeBayar = new ComboBox<>();
    private final ComboBox<String> cmbStatus = new ComboBox<>();

    private final Label lblTotal = new Label("Total: Rp 0");
    private final Map<Integer, Object> pilihanOpsi = new HashMap<>();

    // ── Searchable menu picker ──
    private final ObservableList<MenuItem> allMenuItems = FXCollections.observableArrayList();
    private final FilteredList<MenuItem> filteredMenu = new FilteredList<>(allMenuItems, p -> true);
    private final TextField txtSearchMenu = new TextField();
    private final ListView<MenuItem> listViewMenu = new ListView<>();
    private final Spinner<Integer> spnJumlah = new Spinner<>(1, 99, 1);

    private MenuItem selectedMenuItem = null;

    public TransaksiPanel() {
        setPadding(new Insets(20));
        setSpacing(10);

        Label title = new Label("Transaksi");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox mainLayout = new HBox(15);
        VBox leftPane = buildOrderPane();
        VBox rightPane = buildRiwayatPane();
        leftPane.setPrefWidth(420);
        rightPane.setPrefWidth(430);
        mainLayout.getChildren().addAll(leftPane, rightPane);

        getChildren().addAll(title, mainLayout);
        loadData();
    }

    // ── ORDER PANE ────────────────────────────────────────────────
    private VBox buildOrderPane() {
        txtNamaPelanggan.setPromptText("Nama pelanggan (opsional)");

        txtSearchMenu.setPromptText("🔍 Cari menu...");
        txtSearchMenu.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase().trim();
            filteredMenu.setPredicate(item -> lower.isEmpty() || item.getNama().toLowerCase().contains(lower) ||
                    item.getTipe().toLowerCase().contains(lower));
        });

        listViewMenu.setItems(filteredMenu);
        listViewMenu.setPrefHeight(130);
        listViewMenu.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : String.format("%s  —  Rp %.0f  [%s]", item.getNama(), item.getHarga(), item.getTipe()));
            }
        });

        listViewMenu.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedMenuItem = n;
                loadOpsiForm();
            }
        });

        spnJumlah.setEditable(true);
        spnJumlah.setPrefWidth(70);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Pelanggan:"), txtNamaPelanggan);
        grid.addRow(1, new Label("Jumlah:"), spnJumlah);

        ScrollPane opsiScroll = new ScrollPane(opsiContainer);
        opsiScroll.setFitToWidth(true);
        opsiScroll.setPrefHeight(130);
        opsiScroll.setStyle("-fx-background-color: transparent;");

        Button btnTambahKeranjang = new Button("✚ Tambah ke Keranjang");
        btnTambahKeranjang.setMaxWidth(Double.MAX_VALUE);
        btnTambahKeranjang.setOnAction(e -> tambahKeKeranjang());

        // Keranjang table
        TableColumn<ItemKeranjang, String> colNama = new TableColumn<>("Menu");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaMenu"));
        colNama.setPrefWidth(120);

        TableColumn<ItemKeranjang, Integer> colJumlah = new TableColumn<>("Qty");
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colJumlah.setPrefWidth(40);

        TableColumn<ItemKeranjang, String> colOpsi = new TableColumn<>("Opsi");
        colOpsi.setCellValueFactory(new PropertyValueFactory<>("keteranganOpsi"));
        colOpsi.setPrefWidth(120);

        TableColumn<ItemKeranjang, Double> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %.0f", val));
            }
        });
        colSubtotal.setPrefWidth(90);

        tableKeranjang.getColumns().addAll(colNama, colJumlah, colOpsi, colSubtotal);
        tableKeranjang.setItems(keranjang);
        tableKeranjang.setPrefHeight(130);

        Button btnHapusItem = new Button("✖ Hapus Item");
        btnHapusItem.setOnAction(e -> {
            ItemKeranjang selected = tableKeranjang.getSelectionModel().getSelectedItem();
            if (selected != null) {
                keranjang.remove(selected);
                updateTotal();
            }
        });

        lblTotal.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        cmbMetodeBayar.setItems(FXCollections.observableArrayList("Cash", "QRIS", "Transfer"));
        cmbMetodeBayar.setValue("Cash");

        cmbStatus.setItems(FXCollections.observableArrayList("LUNAS", "BELUM_LUNAS"));
        cmbStatus.setValue("LUNAS");

        Button btnBayar = new Button("✔ Bayar");
        btnBayar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        btnBayar.setMaxWidth(Double.MAX_VALUE);
        btnBayar.setOnAction(e -> prosesBayar());

        Button btnBatal = new Button("✖ Batal");
        btnBatal.setMaxWidth(Double.MAX_VALUE);
        btnBatal.setOnAction(e -> resetOrder());

        GridPane gridBayar = new GridPane();
        gridBayar.setHgap(10);
        gridBayar.setVgap(8);
        gridBayar.addRow(0, new Label("Metode:"), cmbMetodeBayar);
        gridBayar.addRow(1, new Label("Status:"), cmbStatus);

        Label lblMenuTitle = new Label("Pilih Menu:");
        lblMenuTitle.setStyle("-fx-font-weight: bold;");
        Label lblOpsiTitle = new Label("Opsi:");
        lblOpsiTitle.setStyle("-fx-font-weight: bold;");

        return new VBox(6,
                new Label("Form Pesanan:"), grid,
                lblMenuTitle, txtSearchMenu, listViewMenu,
                lblOpsiTitle, opsiScroll,
                btnTambahKeranjang,
                new Separator(),
                new Label("Keranjang:"), tableKeranjang, btnHapusItem,
                new Separator(),
                lblTotal, gridBayar, btnBayar, btnBatal);
    }

    // ── RIWAYAT PANE ──────────────────────────────────────────────
    private VBox buildRiwayatPane() {
        TableColumn<Transaksi, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(40);

        TableColumn<Transaksi, String> colTanggal = new TableColumn<>("Tanggal");
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colTanggal.setPrefWidth(130);

        TableColumn<Transaksi, String> colNama = new TableColumn<>("Pelanggan");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan"));
        colNama.setPrefWidth(100);

        TableColumn<Transaksi, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %.0f", val));
            }
        });
        colTotal.setPrefWidth(90);

        TableColumn<Transaksi, String> colMetode = new TableColumn<>("Metode");
        colMetode.setCellValueFactory(new PropertyValueFactory<>("metodeBayar"));
        colMetode.setPrefWidth(70);

        TableColumn<Transaksi, StatusPembayaran> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusPembayaran"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(StatusPembayaran val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    setStyle("");
                } else {
                    boolean lunas = val == StatusPembayaran.LUNAS;
                    setText(lunas ? "✓ Lunas" : "⏳ Belum");
                    setStyle(lunas ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
        colStatus.setPrefWidth(80);

        tableTransaksi.getColumns().addAll(colId, colTanggal, colNama, colTotal, colMetode, colStatus);
        tableTransaksi.setItems(dataTransaksi);

        Button btnLunas = new Button("✔ Tandai Lunas");
        btnLunas.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnLunas.setOnAction(e -> {
            Transaksi selected = tableTransaksi.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Pilih transaksi dulu!");
                return;
            }
            transaksiDao.updateStatus(selected.getId(), "LUNAS");
            dataTransaksi.setAll(transaksiDao.getAll());
            tableTransaksi.refresh();
        });

        Button btnHapus = new Button("✖ Hapus");
        btnHapus.setOnAction(e -> {
            Transaksi selected = tableTransaksi.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Pilih transaksi dulu!");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Hapus transaksi ini?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    transaksiDao.delete(selected.getId());
                    loadData();
                }
            });
        });

        HBox buttons = new HBox(8, btnLunas, btnHapus);
        Label lbl = new Label("Riwayat Transaksi");
        lbl.setStyle("-fx-font-weight: bold;");

        return new VBox(8, lbl, tableTransaksi, buttons);
    }

    // ── OPSI FORM ─────────────────────────────────────────────────
    private void loadOpsiForm() {
        opsiContainer.getChildren().clear();
        pilihanOpsi.clear();

        if (selectedMenuItem == null)
            return;

        List<OpsiMenu> opsiList = opsiDao.getByMenuId(selectedMenuItem.getId());
        if (opsiList.isEmpty()) {
            opsiContainer.getChildren().add(new Label("(Tidak ada opsi)"));
            return;
        }

        for (OpsiMenu opsi : opsiList) {
            if (opsi.getTipeOpsi().equals("checkbox")) {
                CheckBox chk = new CheckBox(opsi.getNamaOpsi() +
                        (opsi.getHargaTambahan() > 0
                                ? String.format(" (+Rp %.0f)", opsi.getHargaTambahan())
                                : ""));
                chk.setOnAction(ev -> updateTotal());
                pilihanOpsi.put(opsi.getId(), chk);
                opsiContainer.getChildren().add(chk);
            } else {
                List<PilihanOpsi> pilihanList = opsiDao.getPilihanByOpsiId(opsi.getId());
                ComboBox<PilihanOpsi> cmb = new ComboBox<>();
                cmb.setItems(FXCollections.observableArrayList(pilihanList));
                cmb.setPromptText("Pilih " + opsi.getNamaOpsi() + (opsi.isWajib() ? " *" : ""));
                cmb.setMaxWidth(Double.MAX_VALUE);
                cmb.setOnAction(ev -> updateTotal());
                pilihanOpsi.put(opsi.getId(), cmb);
                opsiContainer.getChildren().addAll(new Label(opsi.getNamaOpsi() + ":"), cmb);
            }
        }
    }

    // ── KERANJANG ─────────────────────────────────────────────────
    private void tambahKeKeranjang() {
        if (selectedMenuItem == null) {
            showAlert("Pilih menu dulu!");
            return;
        }

        List<OpsiMenu> opsiList = opsiDao.getByMenuId(selectedMenuItem.getId());

        // Validate wajib pilihan
        for (OpsiMenu opsi : opsiList) {
            if (opsi.isWajib() && opsi.getTipeOpsi().equals("pilihan")) {
                ComboBox<?> cmb = (ComboBox<?>) pilihanOpsi.get(opsi.getId());
                if (cmb != null && cmb.getValue() == null) {
                    showAlert("Opsi \"" + opsi.getNamaOpsi() + "\" wajib dipilih!");
                    return;
                }
            }
        }

        int jumlah = spnJumlah.getValue();
        double hargaTambahan = hitungHargaOpsi(opsiList);
        double subtotal = (selectedMenuItem.getHarga() + hargaTambahan) * jumlah;
        String keterangan = buildKeteranganOpsi(opsiList);

        // Collect chosen IDs for stock deduction later
        List<Integer> checkedOpsiIds = new ArrayList<>();
        List<Integer> chosenPilihanIds = new ArrayList<>();
        for (OpsiMenu opsi : opsiList) {
            if (opsi.getTipeOpsi().equals("checkbox")) {
                CheckBox chk = (CheckBox) pilihanOpsi.get(opsi.getId());
                if (chk != null && chk.isSelected())
                    checkedOpsiIds.add(opsi.getId());
            } else {
                ComboBox<?> cmb = (ComboBox<?>) pilihanOpsi.get(opsi.getId());
                if (cmb != null && cmb.getValue() instanceof PilihanOpsi p)
                    chosenPilihanIds.add(p.getId());
            }
        }

        keranjang.add(new ItemKeranjang(
                selectedMenuItem, jumlah, subtotal, keterangan,
                checkedOpsiIds, chosenPilihanIds));
        updateTotal();
        resetPilihMenu();
    }

    private double hitungHargaOpsi(List<OpsiMenu> opsiList) {
        double total = 0;
        for (OpsiMenu opsi : opsiList) {
            if (opsi.getTipeOpsi().equals("checkbox")) {
                CheckBox chk = (CheckBox) pilihanOpsi.get(opsi.getId());
                if (chk != null && chk.isSelected())
                    total += opsi.getHargaTambahan();
            } else {
                ComboBox<?> cmb = (ComboBox<?>) pilihanOpsi.get(opsi.getId());
                if (cmb != null && cmb.getValue() instanceof PilihanOpsi p)
                    total += p.getHargaTambahan();
            }
        }
        return total;
    }

    private String buildKeteranganOpsi(List<OpsiMenu> opsiList) {
        StringBuilder sb = new StringBuilder();
        for (OpsiMenu opsi : opsiList) {
            if (opsi.getTipeOpsi().equals("checkbox")) {
                CheckBox chk = (CheckBox) pilihanOpsi.get(opsi.getId());
                if (chk != null && chk.isSelected()) {
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append(opsi.getNamaOpsi());
                }
            } else {
                ComboBox<?> cmb = (ComboBox<?>) pilihanOpsi.get(opsi.getId());
                if (cmb != null && cmb.getValue() instanceof PilihanOpsi p) {
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append(opsi.getNamaOpsi()).append(": ").append(p.getLabel());
                }
            }
        }
        return sb.toString();
    }

    private void updateTotal() {
        double total = keranjang.stream().mapToDouble(ItemKeranjang::getSubtotal).sum();
        lblTotal.setText(String.format("Total: Rp %.0f", total));
    }

    // ── BAYAR ─────────────────────────────────────────────────────
    private void prosesBayar() {
        if (keranjang.isEmpty()) {
            showAlert("Keranjang kosong!");
            return;
        }

        String tanggal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        double total = keranjang.stream().mapToDouble(ItemKeranjang::getSubtotal).sum();
        String metode = cmbMetodeBayar.getValue();
        String nama = txtNamaPelanggan.getText().trim();

        Transaksi t = new Transaksi(0, nama.isEmpty() ? "-" : nama,
                tanggal, total, metode, StatusPembayaran.valueOf(cmbStatus.getValue()));

        // Build detail and opsi ID lists in parallel
        List<DetailTransaksi> details = new ArrayList<>();
        List<List<Integer>> checkedOpsiIdsList = new ArrayList<>();
        List<List<Integer>> chosenPilihanIdsList = new ArrayList<>();

        for (ItemKeranjang item : keranjang) {
            details.add(new DetailTransaksi(
                    0, 0, item.getMenu().getId(), item.getJumlah(), item.getSubtotal()));
            checkedOpsiIdsList.add(item.getCheckedOpsiIds());
            chosenPilihanIdsList.add(item.getChosenPilihanIds());
        }

        int transaksiId = transaksiDao.simpanTransaksiLengkap(
                t, details, opsiDao, checkedOpsiIdsList, chosenPilihanIdsList);

        if (transaksiId == -1) {
            showAlert("Transaksi gagal! Stok tidak cukup atau terjadi kesalahan.\nTidak ada data yang tersimpan.");
            return;
        }

        showAlert("Transaksi berhasil! Total: " + String.format("Rp %.0f", total));
        resetOrder();
        loadData();
    }

    // ── HELPERS ───────────────────────────────────────────────────
    public void loadData() {
        dataTransaksi.setAll(transaksiDao.getAll());
        allMenuItems.setAll(menuDao.getAll());
    }

    private void resetPilihMenu() {
        opsiContainer.getChildren().clear();
        pilihanOpsi.clear();
        spnJumlah.getValueFactory().setValue(1);
        listViewMenu.getSelectionModel().clearSelection();
        txtSearchMenu.clear();
        selectedMenuItem = null;
    }

    private void resetOrder() {
        keranjang.clear();
        txtNamaPelanggan.clear();
        opsiContainer.getChildren().clear();
        pilihanOpsi.clear();
        spnJumlah.getValueFactory().setValue(1);
        lblTotal.setText("Total: Rp 0");
        listViewMenu.getSelectionModel().clearSelection();
        txtSearchMenu.clear();
        selectedMenuItem = null;
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    // ── INNER CLASS ───────────────────────────────────────────────
    public static class ItemKeranjang {
        private final MenuItem menu;
        private final int jumlah;
        private final double subtotal;
        private final String keteranganOpsi;
        private final List<Integer> checkedOpsiIds;
        private final List<Integer> chosenPilihanIds;

        public ItemKeranjang(MenuItem menu, int jumlah, double subtotal, String keteranganOpsi,
                List<Integer> checkedOpsiIds, List<Integer> chosenPilihanIds) {
            this.menu = menu;
            this.jumlah = jumlah;
            this.subtotal = subtotal;
            this.keteranganOpsi = keteranganOpsi;
            this.checkedOpsiIds = checkedOpsiIds;
            this.chosenPilihanIds = chosenPilihanIds;
        }

        public MenuItem getMenu() {
            return menu;
        }

        public String getNamaMenu() {
            return menu.getNama();
        }

        public int getJumlah() {
            return jumlah;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public String getKeteranganOpsi() {
            return keteranganOpsi;
        }

        public List<Integer> getCheckedOpsiIds() {
            return checkedOpsiIds;
        }

        public List<Integer> getChosenPilihanIds() {
            return chosenPilihanIds;
        }
    }
}