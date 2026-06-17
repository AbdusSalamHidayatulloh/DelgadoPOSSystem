package com.testproject.ui.transaksi;

// 1. Import Model secara spesifik
import com.testproject.model.DetailTransaksi;
import com.testproject.model.MenuItem;
import com.testproject.model.OpsiMenu;
import com.testproject.model.PilihanOpsi;
import com.testproject.model.StatusPembayaran;
import com.testproject.model.Transaksi;
import com.testproject.service.MenuService;
import com.testproject.service.TransaksiService;
import com.testproject.utils.UIHelper;

// 2. Import JavaFX secara spesifik
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

// 3. Import Java Utils
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderPane extends VBox {
    private final TransaksiService transaksiService;
    private final MenuService menuService = new MenuService(); // Daur ulang service dari Menu!
    private final Runnable onCheckoutSuccess;

    private final TextField txtNamaPelanggan = new TextField();
    private final TextField txtSearchMenu = new TextField();
    private final ListView<MenuItem> listViewMenu = new ListView<>();
    private final Spinner<Integer> spnJumlah = new Spinner<>(1, 99, 1);
    private final VBox opsiContainer = new VBox(8);
    private final Label lblTotal = new Label("Total: Rp 0");
    private final ComboBox<String> cmbMetodeBayar = new ComboBox<>();
    private final ComboBox<String> cmbStatus = new ComboBox<>();

    private final TableView<ItemKeranjang> tableKeranjang = new TableView<>();
    private final ObservableList<ItemKeranjang> keranjang = FXCollections.observableArrayList();
    private final ObservableList<MenuItem> allMenuItems = FXCollections.observableArrayList();
    private final FilteredList<MenuItem> filteredMenu = new FilteredList<>(allMenuItems, p -> true);

    private final Map<Integer, Object> pilihanOpsi = new HashMap<>();
    private MenuItem selectedMenuItem = null;

    public OrderPane(TransaksiService transaksiService, Runnable onCheckoutSuccess) {
        this.transaksiService = transaksiService;
        this.onCheckoutSuccess = onCheckoutSuccess;
        setSpacing(6);

        txtNamaPelanggan.setPromptText("Nama pelanggan (opsional)");
        txtSearchMenu.setPromptText("🔍 Cari menu...");
        txtSearchMenu.textProperty().addListener((obs, old, val) -> {
            String lower = val.toLowerCase().trim();
            filteredMenu.setPredicate(item -> lower.isEmpty() || item.getNama().toLowerCase().contains(lower) || item.getTipe().toLowerCase().contains(lower));
        });

        listViewMenu.setItems(filteredMenu);
        listViewMenu.setPrefHeight(130);
        listViewMenu.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%s  —  Rp %.0f  [%s]", item.getNama(), item.getHarga(), item.getTipe()));
            }
        });

        listViewMenu.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) { selectedMenuItem = n; loadOpsiForm(); }
        });

        spnJumlah.setEditable(true); spnJumlah.setPrefWidth(70);
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(8);
        grid.addRow(0, new Label("Pelanggan:"), txtNamaPelanggan);
        grid.addRow(1, new Label("Jumlah:"), spnJumlah);

        ScrollPane opsiScroll = new ScrollPane(opsiContainer);
        opsiScroll.setFitToWidth(true); opsiScroll.setPrefHeight(130);

        Button btnTambahKeranjang = new Button("✚ Tambah ke Keranjang");
        btnTambahKeranjang.setMaxWidth(Double.MAX_VALUE);
        btnTambahKeranjang.setOnAction(e -> tambahKeKeranjang());

        // Keranjang Table
        TableColumn<ItemKeranjang, String> colNama = new TableColumn<>("Menu"); colNama.setCellValueFactory(new PropertyValueFactory<>("namaMenu")); colNama.setPrefWidth(120);
        TableColumn<ItemKeranjang, Integer> colJumlah = new TableColumn<>("Qty"); colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah")); colJumlah.setPrefWidth(40);
        TableColumn<ItemKeranjang, String> colOpsi = new TableColumn<>("Opsi"); colOpsi.setCellValueFactory(new PropertyValueFactory<>("keteranganOpsi")); colOpsi.setPrefWidth(120);
        TableColumn<ItemKeranjang, Double> colSubtotal = new TableColumn<>("Subtotal"); colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal")); colSubtotal.setCellFactory(col -> new UIHelper.FormatDesimalCell<>()); colSubtotal.setPrefWidth(90);
        
        tableKeranjang.getColumns().addAll(colNama, colJumlah, colOpsi, colSubtotal);
        tableKeranjang.setItems(keranjang); tableKeranjang.setPrefHeight(130);

        Button btnHapusItem = new Button("✖ Hapus Item");
        btnHapusItem.setOnAction(e -> {
            ItemKeranjang selected = tableKeranjang.getSelectionModel().getSelectedItem();
            if (selected != null) { keranjang.remove(selected); updateTotal(); }
        });

        lblTotal.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        cmbMetodeBayar.setItems(FXCollections.observableArrayList("Cash", "QRIS", "Transfer")); cmbMetodeBayar.setValue("Cash");
        cmbStatus.setItems(FXCollections.observableArrayList("LUNAS", "BELUM_LUNAS")); cmbStatus.setValue("LUNAS");

        Button btnBayar = new Button("✔ Bayar"); btnBayar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;"); btnBayar.setMaxWidth(Double.MAX_VALUE);
        btnBayar.setOnAction(e -> prosesBayar());
        Button btnBatal = new Button("✖ Batal"); btnBatal.setMaxWidth(Double.MAX_VALUE); btnBatal.setOnAction(e -> resetOrder());

        GridPane gridBayar = new GridPane(); gridBayar.setHgap(10); gridBayar.setVgap(8);
        gridBayar.addRow(0, new Label("Metode:"), cmbMetodeBayar); gridBayar.addRow(1, new Label("Status:"), cmbStatus);

        getChildren().addAll(
            new Label("Form Pesanan:"), grid,
            new Label("Pilih Menu:"), txtSearchMenu, listViewMenu,
            new Label("Opsi:"), opsiScroll, btnTambahKeranjang, new Separator(),
            new Label("Keranjang:"), tableKeranjang, btnHapusItem, new Separator(),
            lblTotal, gridBayar, btnBayar, btnBatal
        );
    }

    public void loadMenuData() { allMenuItems.setAll(menuService.ambilSemuaMenu()); }

    private void loadOpsiForm() {
        opsiContainer.getChildren().clear(); pilihanOpsi.clear();
        if (selectedMenuItem == null) return;
        List<OpsiMenu> opsiList = menuService.ambilOpsiByMenu(selectedMenuItem.getId());
        if (opsiList.isEmpty()) { opsiContainer.getChildren().add(new Label("(Tidak ada opsi)")); return; }

        for (OpsiMenu opsi : opsiList) {
            if (opsi.getTipeOpsi().equals("checkbox")) {
                CheckBox chk = new CheckBox(opsi.getNamaOpsi() + (opsi.getHargaTambahan() > 0 ? String.format(" (+Rp %.0f)", opsi.getHargaTambahan()) : ""));
                chk.setOnAction(ev -> updateTotal());
                pilihanOpsi.put(opsi.getId(), chk);
                opsiContainer.getChildren().add(chk);
            } else {
                List<PilihanOpsi> pilihanList = menuService.ambilPilihanByOpsi(opsi.getId());
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

    private void tambahKeKeranjang() {
        if (selectedMenuItem == null) { UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih menu dulu!"); return; }
        List<OpsiMenu> opsiList = menuService.ambilOpsiByMenu(selectedMenuItem.getId());
        for (OpsiMenu opsi : opsiList) {
            if (opsi.isWajib() && opsi.getTipeOpsi().equals("pilihan")) {
                ComboBox<?> cmb = (ComboBox<?>) pilihanOpsi.get(opsi.getId());
                if (cmb != null && cmb.getValue() == null) { UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Opsi \"" + opsi.getNamaOpsi() + "\" wajib dipilih!"); return; }
            }
        }

        int jumlah = spnJumlah.getValue();
        double subtotal = (selectedMenuItem.getHarga() + hitungHargaOpsi(opsiList)) * jumlah;
        String keterangan = buildKeteranganOpsi(opsiList);

        List<Integer> checkedOpsiIds = new ArrayList<>();
        List<Integer> chosenPilihanIds = new ArrayList<>();
        for (OpsiMenu opsi : opsiList) {
            if (opsi.getTipeOpsi().equals("checkbox")) {
                CheckBox chk = (CheckBox) pilihanOpsi.get(opsi.getId());
                if (chk != null && chk.isSelected()) checkedOpsiIds.add(opsi.getId());
            } else {
                ComboBox<?> cmb = (ComboBox<?>) pilihanOpsi.get(opsi.getId());
                if (cmb != null && cmb.getValue() instanceof PilihanOpsi p) chosenPilihanIds.add(p.getId());
            }
        }

        keranjang.add(new ItemKeranjang(selectedMenuItem, jumlah, subtotal, keterangan, checkedOpsiIds, chosenPilihanIds));
        updateTotal(); resetPilihMenu();
    }

    private double hitungHargaOpsi(List<OpsiMenu> opsiList) {
        double total = 0;
        for (OpsiMenu opsi : opsiList) {
            if (opsi.getTipeOpsi().equals("checkbox")) {
                CheckBox chk = (CheckBox) pilihanOpsi.get(opsi.getId());
                if (chk != null && chk.isSelected()) total += opsi.getHargaTambahan();
            } else {
                ComboBox<?> cmb = (ComboBox<?>) pilihanOpsi.get(opsi.getId());
                if (cmb != null && cmb.getValue() instanceof PilihanOpsi p) total += p.getHargaTambahan();
            }
        }
        return total;
    }

    private String buildKeteranganOpsi(List<OpsiMenu> opsiList) {
        StringBuilder sb = new StringBuilder();
        for (OpsiMenu opsi : opsiList) {
            if (opsi.getTipeOpsi().equals("checkbox")) {
                CheckBox chk = (CheckBox) pilihanOpsi.get(opsi.getId());
                if (chk != null && chk.isSelected()) { if (sb.length() > 0) sb.append(", "); sb.append(opsi.getNamaOpsi()); }
            } else {
                ComboBox<?> cmb = (ComboBox<?>) pilihanOpsi.get(opsi.getId());
                if (cmb != null && cmb.getValue() instanceof PilihanOpsi p) { if (sb.length() > 0) sb.append(", "); sb.append(opsi.getNamaOpsi()).append(": ").append(p.getLabel()); }
            }
        }
        return sb.toString();
    }

    private void updateTotal() {
        double total = keranjang.stream().mapToDouble(ItemKeranjang::getSubtotal).sum();
        lblTotal.setText(String.format("Total: Rp %.0f", total));
    }

    private void prosesBayar() {
        if (keranjang.isEmpty()) { UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Keranjang kosong!"); return; }
        String tanggal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        double total = keranjang.stream().mapToDouble(ItemKeranjang::getSubtotal).sum();
        String metode = cmbMetodeBayar.getValue();
        String nama = txtNamaPelanggan.getText().trim();

        Transaksi t = new Transaksi(0, nama.isEmpty() ? "-" : nama, tanggal, total, metode, StatusPembayaran.valueOf(cmbStatus.getValue()));
        List<DetailTransaksi> details = new ArrayList<>();
        List<List<Integer>> checkedOpsiIdsList = new ArrayList<>();
        List<List<Integer>> chosenPilihanIdsList = new ArrayList<>();

        for (ItemKeranjang item : keranjang) {
            details.add(new DetailTransaksi(0, 0, item.getMenu().getId(), item.getJumlah(), item.getSubtotal()));
            checkedOpsiIdsList.add(item.getCheckedOpsiIds());
            chosenPilihanIdsList.add(item.getChosenPilihanIds());
        }

        boolean sukses = transaksiService.prosesPembayaran(t, details, checkedOpsiIdsList, chosenPilihanIdsList);
        if (!sukses) { UIHelper.showAlert(Alert.AlertType.ERROR, "Error", "Transaksi gagal! Stok bahan tidak cukup atau terjadi kesalahan internal."); return; }

        UIHelper.showAlert(Alert.AlertType.INFORMATION, "Sukses", "Transaksi berhasil! Total: Rp " + (int)total);
        resetOrder(); onCheckoutSuccess.run();
    }

    private void resetPilihMenu() {
        opsiContainer.getChildren().clear(); pilihanOpsi.clear();
        spnJumlah.getValueFactory().setValue(1);
        listViewMenu.getSelectionModel().clearSelection();
        txtSearchMenu.clear(); selectedMenuItem = null;
    }

    private void resetOrder() {
        keranjang.clear(); txtNamaPelanggan.clear(); resetPilihMenu(); lblTotal.setText("Total: Rp 0");
    }
}