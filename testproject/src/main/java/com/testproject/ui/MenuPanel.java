package com.testproject.ui;

import com.testproject.dao.BahanDao;
import com.testproject.dao.MenuDao;
import com.testproject.dao.OpsiMenuDao;
import com.testproject.model.Bahan;
import com.testproject.model.MenuItem;
import com.testproject.model.OpsiMenu;
import com.testproject.model.PilihanOpsi;
import com.testproject.model.ResepItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class MenuPanel extends VBox {

    private final MenuDao menuDao       = new MenuDao();
    private final OpsiMenuDao opsiDao   = new OpsiMenuDao();
    private final BahanDao bahanDao     = new BahanDao();

    private final TableView<MenuItem> tableMenu             = new TableView<>();
    private final ObservableList<MenuItem> dataMenu         = FXCollections.observableArrayList();

    private final TableView<OpsiMenu> tableOpsi             = new TableView<>();
    private final ObservableList<OpsiMenu> dataOpsi         = FXCollections.observableArrayList();

    private final TableView<PilihanOpsi> tablePilihan       = new TableView<>();
    private final ObservableList<PilihanOpsi> dataPilihan   = FXCollections.observableArrayList();

    private final TableView<ResepItem> tableResep           = new TableView<>();
    private final ObservableList<ResepItem> dataResep       = FXCollections.observableArrayList();

    private final TableView<ResepItem> tableResepOpsi       = new TableView<>();
    private final ObservableList<ResepItem> dataResepOpsi   = FXCollections.observableArrayList();

    private final TableView<ResepItem> tableResepPilihan    = new TableView<>();
    private final ObservableList<ResepItem> dataResepPilihan = FXCollections.observableArrayList();

    // Menu form
    private final TextField txtNamaMenu  = new TextField();
    private final TextField txtHargaMenu = new TextField();
    private final ComboBox<String> cmbTipe = new ComboBox<>();

    // Opsi form
    private final TextField txtNamaOpsi        = new TextField();
    private final ComboBox<String> cmbTipeOpsi = new ComboBox<>();
    private final CheckBox chkWajib            = new CheckBox("Wajib");
    private final TextField txtHargaOpsi       = new TextField();

    // Pilihan form
    private final TextField txtLabelPilihan  = new TextField();
    private final TextField txtHargaPilihan  = new TextField();

    // Resep menu form
    private final ComboBox<Bahan> cmbBahanResep = new ComboBox<>();
    private final TextField txtJumlahResep       = new TextField();

    // Resep opsi form
    private final ComboBox<Bahan> cmbBahanResepOpsi = new ComboBox<>();
    private final TextField txtJumlahResepOpsi       = new TextField();

    // Resep pilihan form
    private final ComboBox<Bahan> cmbBahanResepPilihan = new ComboBox<>();
    private final TextField txtJumlahResepPilihan       = new TextField();

    // Pilihan button (needs class-level ref for dynamic label)
    private Button btnTambahPilihan;

    private MenuItem selectedMenu       = null;
    private OpsiMenu selectedOpsi       = null;
    private PilihanOpsi selectedPilihan = null;

    public MenuPanel() {
        setPadding(new Insets(20));
        setSpacing(10);

        Label title = new Label("Manajemen Menu");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox mainLayout = new HBox(15);
        VBox leftPane = buildMenuPane();

        TabPane rightTabs = new TabPane();
        rightTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab tabOpsi  = new Tab("⚙ Opsi Menu",     buildOpsiPane());
        Tab tabResep = new Tab("📋 Resep & Bahan", buildResepPane());
        rightTabs.getTabs().addAll(tabOpsi, tabResep);
        rightTabs.setPrefWidth(520);

        leftPane.setPrefWidth(350);
        mainLayout.getChildren().addAll(leftPane, rightTabs);

        getChildren().addAll(title, mainLayout);
        loadData();
    }

    // ── MENU PANE ────────────────────────────────────────────────
    private VBox buildMenuPane() {
        TableColumn<MenuItem, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNama.setPrefWidth(120);

        TableColumn<MenuItem, Double> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %.0f", val));
            }
        });
        colHarga.setPrefWidth(90);

        TableColumn<MenuItem, String> colTipe = new TableColumn<>("Tipe");
        colTipe.setCellValueFactory(new PropertyValueFactory<>("tipe"));
        colTipe.setPrefWidth(80);

        TableColumn<MenuItem, String> colResep = new TableColumn<>("Resep");
        colResep.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colResep.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setText(null); setStyle(""); }
                else {
                    MenuItem m = getTableRow().getItem();
                    boolean hasResep = !bahanDao.getResepByMenuId(m.getId()).isEmpty();
                    setText(hasResep ? "✓" : "-");
                    setStyle(hasResep ? "-fx-text-fill: green;" : "-fx-text-fill: #aaa;");
                }
            }
        });
        colResep.setPrefWidth(50);

        tableMenu.getColumns().addAll(colNama, colHarga, colTipe, colResep);
        tableMenu.setItems(dataMenu);
        tableMenu.setPrefHeight(200);

        tableMenu.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedMenu = n;
                txtNamaMenu.setText(n.getNama());
                txtHargaMenu.setText(String.valueOf(n.getHarga()));
                cmbTipe.setValue(n.getTipe());
                loadOpsi(n.getId());
                loadResep(n.getId());
            }
        });

        txtNamaMenu.setPromptText("Nama menu");
        txtHargaMenu.setPromptText("Harga");
        cmbTipe.setItems(FXCollections.observableArrayList("Makanan", "Minuman"));
        cmbTipe.setValue("Makanan");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.addRow(0, new Label("Nama:"), txtNamaMenu);
        grid.addRow(1, new Label("Harga:"), txtHargaMenu);
        grid.addRow(2, new Label("Tipe:"), cmbTipe);

        Button btnTambah = new Button("✚ Tambah");
        Button btnUpdate = new Button("✎ Update");
        Button btnHapus  = new Button("✖ Hapus");
        Button btnReset  = new Button("Reset");

        btnTambah.setOnAction(e -> {
            if (selectedMenu != null) { showAlert("Tekan Reset untuk tambah menu baru!"); return; }
            MenuItem m = getMenuFromForm();
            if (m != null) { menuDao.add(m); loadData(); resetMenuForm(); }
        });

        btnUpdate.setOnAction(e -> {
            if (selectedMenu == null) { showAlert("Pilih menu dulu!"); return; }
            MenuItem m = getMenuFromForm();
            if (m != null) { m.setId(selectedMenu.getId()); menuDao.update(m); loadData(); resetMenuForm(); }
        });

        btnHapus.setOnAction(e -> {
            if (selectedMenu == null) { showAlert("Pilih menu dulu!"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hapus menu \"" + selectedMenu.getNama() + "\" beserta semua opsi dan resepnya?",
                ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    opsiDao.deleteByMenuId(selectedMenu.getId());
                    menuDao.delete(selectedMenu.getId());
                    loadData();
                    resetMenuForm();
                }
            });
        });

        btnReset.setOnAction(e -> resetMenuForm());

        HBox buttons = new HBox(8, btnTambah, btnUpdate, btnHapus, btnReset);
        Label lbl = new Label("Daftar Menu");
        lbl.setStyle("-fx-font-weight: bold;");

        return new VBox(8, lbl, tableMenu, new Label("Form Menu:"), grid, buttons);
    }

    // ── RESEP MENU PANE ──────────────────────────────────────────
    private VBox buildResepPane() {
        TableColumn<ResepItem, String> colBahan = new TableColumn<>("Bahan");
        colBahan.setCellValueFactory(new PropertyValueFactory<>("namaBahan"));
        colBahan.setPrefWidth(140);

        TableColumn<ResepItem, Double> colJumlah = new TableColumn<>("Jumlah Pakai");
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahDipakai"));
        colJumlah.setPrefWidth(100);

        TableColumn<ResepItem, String> colSatuan = new TableColumn<>("Satuan");
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colSatuan.setPrefWidth(80);

        tableResep.getColumns().addAll(colBahan, colJumlah, colSatuan);
        tableResep.setItems(dataResep);
        tableResep.setPrefHeight(160);

        cmbBahanResep.setItems(FXCollections.observableArrayList(bahanDao.getAll()));
        cmbBahanResep.setPromptText("Pilih bahan...");
        cmbBahanResep.setMaxWidth(Double.MAX_VALUE);
        txtJumlahResep.setPromptText("Jumlah dipakai per porsi");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.addRow(0, new Label("Bahan:"), cmbBahanResep);
        grid.addRow(1, new Label("Jumlah:"), txtJumlahResep);

        Button btnTambahResep = new Button("✚ Tambah ke Resep");
        Button btnHapusResep  = new Button("✖ Hapus dari Resep");

        btnTambahResep.setOnAction(e -> {
            if (selectedMenu == null) { showAlert("Pilih menu dulu dari tabel kiri!"); return; }
            Bahan bahan = cmbBahanResep.getValue();
            if (bahan == null) { showAlert("Pilih bahan!"); return; }
            try {
                double jumlah = Double.parseDouble(txtJumlahResep.getText().trim());
                if (jumlah <= 0) { showAlert("Jumlah harus lebih dari 0!"); return; }
                bahanDao.addResep(selectedMenu.getId(), bahan.getId(), jumlah);
                loadResep(selectedMenu.getId());
                cmbBahanResep.setValue(null);
                txtJumlahResep.clear();
                tableMenu.refresh();
            } catch (NumberFormatException ex) { showAlert("Jumlah harus berupa angka!"); }
        });

        btnHapusResep.setOnAction(e -> {
            ResepItem sel = tableResep.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert("Pilih resep yang ingin dihapus!"); return; }
            bahanDao.deleteResep(sel.getMenuId(), sel.getBahanId());
            loadResep(selectedMenu.getId());
            tableMenu.refresh();
        });

        Label hint = new Label("ℹ Resep menentukan bahan yang otomatis terpotong saat transaksi disimpan.");
        hint.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        hint.setWrapText(true);

        Label lbl = new Label("Resep Menu (Bahan Dasar)");
        lbl.setStyle("-fx-font-weight: bold;");

        VBox pane = new VBox(8,
            lbl, tableResep,
            new Label("Tambah Bahan ke Resep:"), grid,
            new HBox(8, btnTambahResep, btnHapusResep),
            hint);
        pane.setPadding(new Insets(10));
        return pane;
    }

    // ── OPSI PANE ────────────────────────────────────────────────
    private VBox buildOpsiPane() {

        // ── Opsi table ──
        TableColumn<OpsiMenu, String> colNama = new TableColumn<>("Nama Opsi");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaOpsi"));
        colNama.setPrefWidth(120);

        TableColumn<OpsiMenu, String> colTipe = new TableColumn<>("Tipe");
        colTipe.setCellValueFactory(new PropertyValueFactory<>("tipeOpsi"));
        colTipe.setPrefWidth(80);

        TableColumn<OpsiMenu, Boolean> colWajib = new TableColumn<>("Wajib");
        colWajib.setCellValueFactory(new PropertyValueFactory<>("wajib"));
        colWajib.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : val ? "Ya" : "Tidak");
            }
        });
        colWajib.setPrefWidth(60);

        TableColumn<OpsiMenu, Double> colHarga = new TableColumn<>("Harga Tambahan");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaTambahan"));
        colHarga.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %.0f", val));
            }
        });
        colHarga.setPrefWidth(110);

        tableOpsi.getColumns().addAll(colNama, colTipe, colWajib, colHarga);
        tableOpsi.setItems(dataOpsi);
        tableOpsi.setPrefHeight(130);

        tableOpsi.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedOpsi = n;
                txtNamaOpsi.setText(n.getNamaOpsi());
                cmbTipeOpsi.setValue(n.getTipeOpsi());
                chkWajib.setSelected(n.isWajib());
                txtHargaOpsi.setText(String.valueOf(n.getHargaTambahan()));
                loadPilihan(n.getId());
                boolean isCheckbox = n.getTipeOpsi().equals("checkbox");
                txtHargaOpsi.setVisible(isCheckbox);
                txtHargaOpsi.setManaged(isCheckbox);
                if (isCheckbox) dataResepOpsi.setAll(opsiDao.getResepByOpsiId(n.getId()));
                else dataResepOpsi.clear();
                dataResepPilihan.clear();
                selectedPilihan = null;
            }
        });

        txtNamaOpsi.setPromptText("Nama opsi");
        txtHargaOpsi.setPromptText("Harga tambahan (jika checkbox)");
        cmbTipeOpsi.setItems(FXCollections.observableArrayList("checkbox", "pilihan"));
        cmbTipeOpsi.setValue("pilihan");
        cmbTipeOpsi.setOnAction(e -> {
            boolean isCheckbox = cmbTipeOpsi.getValue().equals("checkbox");
            txtHargaOpsi.setVisible(isCheckbox);
            txtHargaOpsi.setManaged(isCheckbox);
        });
        txtHargaOpsi.setVisible(false);
        txtHargaOpsi.setManaged(false);

        GridPane gridOpsi = new GridPane();
        gridOpsi.setHgap(10); gridOpsi.setVgap(8);
        gridOpsi.addRow(0, new Label("Nama:"), txtNamaOpsi, cmbTipeOpsi);
        gridOpsi.addRow(1, chkWajib, txtHargaOpsi);

        Button btnTambahOpsi = new Button("✚ Tambah Opsi");
        Button btnHapusOpsi  = new Button("✖ Hapus Opsi");
        Button btnResetOpsi  = new Button("Reset");

        btnTambahOpsi.setOnAction(e -> {
            if (selectedMenu == null) { showAlert("Pilih menu dulu!"); return; }
            String nama = txtNamaOpsi.getText().trim();
            String tipe = cmbTipeOpsi.getValue();
            if (nama.isEmpty()) { showAlert("Nama opsi tidak boleh kosong!"); return; }
            double hargaOpsi = 0;
            if (tipe.equals("checkbox")) {
                try { hargaOpsi = Double.parseDouble(txtHargaOpsi.getText().trim()); }
                catch (NumberFormatException ex) { showAlert("Harga tambahan harus angka!"); return; }
            }
            opsiDao.add(new OpsiMenu(0, selectedMenu.getId(), nama, tipe, chkWajib.isSelected(), hargaOpsi));
            loadOpsi(selectedMenu.getId());
            resetOpsiForm();
        });

        btnHapusOpsi.setOnAction(e -> {
            if (selectedOpsi == null) { showAlert("Pilih opsi dulu!"); return; }
            opsiDao.delete(selectedOpsi.getId());
            loadOpsi(selectedMenu.getId());
            resetOpsiForm();
        });

        btnResetOpsi.setOnAction(e -> resetOpsiForm());

        // ── Pilihan table ──
        TableColumn<PilihanOpsi, String> colLabel = new TableColumn<>("Label");
        colLabel.setCellValueFactory(new PropertyValueFactory<>("label"));
        colLabel.setPrefWidth(130);

        TableColumn<PilihanOpsi, Double> colHargaPilihan = new TableColumn<>("Harga Tambahan");
        colHargaPilihan.setCellValueFactory(new PropertyValueFactory<>("hargaTambahan"));
        colHargaPilihan.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %.0f", val));
            }
        });
        colHargaPilihan.setPrefWidth(110);

        tablePilihan.getColumns().addAll(colLabel, colHargaPilihan);
        tablePilihan.setItems(dataPilihan);
        tablePilihan.setPrefHeight(110);

        // Initialize button here so listener can reference it
        btnTambahPilihan = new Button("✚ Tambah Pilihan");

        tablePilihan.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            selectedPilihan = n;
            if (n != null) {
                // Populate fields for editing
                txtLabelPilihan.setText(n.getLabel());
                txtHargaPilihan.setText(String.valueOf(n.getHargaTambahan()));
                dataResepPilihan.setAll(opsiDao.getResepByPilihanId(n.getId()));
                btnTambahPilihan.setText("✎ Update Pilihan");
            } else {
                dataResepPilihan.clear();
                btnTambahPilihan.setText("✚ Tambah Pilihan");
            }
        });

        txtLabelPilihan.setPromptText("Label pilihan (contoh: Chicken, Beef)");
        txtHargaPilihan.setPromptText("Harga tambahan (0 jika gratis)");

        GridPane gridPilihan = new GridPane();
        gridPilihan.setHgap(10); gridPilihan.setVgap(8);
        gridPilihan.addRow(0, new Label("Label:"), txtLabelPilihan, new Label("Harga:"), txtHargaPilihan);

        btnTambahPilihan.setOnAction(e -> {
            if (selectedOpsi == null) { showAlert("Pilih opsi dulu!"); return; }
            if (selectedOpsi.getTipeOpsi().equals("checkbox")) {
                showAlert("Opsi checkbox tidak perlu pilihan!"); return;
            }
            String label = txtLabelPilihan.getText().trim();
            if (label.isEmpty()) { showAlert("Label tidak boleh kosong!"); return; }
            double harga = 0;
            try { harga = Double.parseDouble(txtHargaPilihan.getText().trim()); }
            catch (NumberFormatException ex) { harga = 0; }

            if (selectedPilihan != null) {
                // UPDATE existing
                opsiDao.updatePilihan(new PilihanOpsi(
                    selectedPilihan.getId(), selectedOpsi.getId(), label, harga));
            } else {
                // ADD new
                opsiDao.addPilihan(new PilihanOpsi(0, selectedOpsi.getId(), label, harga));
            }
            loadPilihan(selectedOpsi.getId());
            txtLabelPilihan.clear();
            txtHargaPilihan.clear();
            selectedPilihan = null;
            btnTambahPilihan.setText("✚ Tambah Pilihan");
        });

        Button btnHapusPilihan = new Button("✖ Hapus Pilihan");
        btnHapusPilihan.setOnAction(e -> {
            PilihanOpsi sel = tablePilihan.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert("Pilih pilihan dulu!"); return; }
            opsiDao.deletePilihan(sel.getId());
            loadPilihan(selectedOpsi.getId());
            txtLabelPilihan.clear();
            txtHargaPilihan.clear();
            selectedPilihan = null;
            btnTambahPilihan.setText("✚ Tambah Pilihan");
        });

        Button btnResetPilihan = new Button("Reset");
        btnResetPilihan.setOnAction(e -> {
            tablePilihan.getSelectionModel().clearSelection();
            txtLabelPilihan.clear();
            txtHargaPilihan.clear();
            selectedPilihan = null;
            btnTambahPilihan.setText("✚ Tambah Pilihan");
        });

        // ── Resep Opsi (checkbox) section ──
        Label lblResepOpsi = new Label("Resep Opsi Checkbox:");
        lblResepOpsi.setStyle("-fx-font-weight: bold;");

        TableColumn<ResepItem, String> colROBahan = new TableColumn<>("Bahan");
        colROBahan.setCellValueFactory(new PropertyValueFactory<>("namaBahan"));
        colROBahan.setPrefWidth(120);
        TableColumn<ResepItem, Double> colROJumlah = new TableColumn<>("Jumlah");
        colROJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahDipakai"));
        colROJumlah.setPrefWidth(70);
        TableColumn<ResepItem, String> colROSatuan = new TableColumn<>("Satuan");
        colROSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colROSatuan.setPrefWidth(60);
        tableResepOpsi.getColumns().addAll(colROBahan, colROJumlah, colROSatuan);
        tableResepOpsi.setItems(dataResepOpsi);
        tableResepOpsi.setPrefHeight(90);

        cmbBahanResepOpsi.setItems(FXCollections.observableArrayList(bahanDao.getAll()));
        cmbBahanResepOpsi.setPromptText("Pilih bahan...");
        cmbBahanResepOpsi.setMaxWidth(Double.MAX_VALUE);
        txtJumlahResepOpsi.setPromptText("Jumlah per porsi");

        GridPane gridResepOpsi = new GridPane();
        gridResepOpsi.setHgap(8); gridResepOpsi.setVgap(6);
        gridResepOpsi.addRow(0, new Label("Bahan:"), cmbBahanResepOpsi,
                                new Label("Jumlah:"), txtJumlahResepOpsi);

        Button btnTambahResepOpsi = new Button("✚ Tambah");
        Button btnHapusResepOpsi  = new Button("✖ Hapus");

        btnTambahResepOpsi.setOnAction(e -> {
            if (selectedOpsi == null || !selectedOpsi.getTipeOpsi().equals("checkbox")) {
                showAlert("Pilih opsi bertipe checkbox dulu!"); return;
            }
            Bahan b = cmbBahanResepOpsi.getValue();
            if (b == null) { showAlert("Pilih bahan!"); return; }
            try {
                double jml = Double.parseDouble(txtJumlahResepOpsi.getText().trim());
                if (jml <= 0) { showAlert("Jumlah harus lebih dari 0!"); return; }
                opsiDao.addResepOpsi(selectedOpsi.getId(), b.getId(), jml);
                dataResepOpsi.setAll(opsiDao.getResepByOpsiId(selectedOpsi.getId()));
                cmbBahanResepOpsi.setValue(null);
                txtJumlahResepOpsi.clear();
            } catch (NumberFormatException ex) { showAlert("Jumlah harus angka!"); }
        });

        btnHapusResepOpsi.setOnAction(e -> {
            ResepItem sel = tableResepOpsi.getSelectionModel().getSelectedItem();
            if (sel == null || selectedOpsi == null) { showAlert("Pilih resep dulu!"); return; }
            opsiDao.deleteResepOpsi(selectedOpsi.getId(), sel.getBahanId());
            dataResepOpsi.setAll(opsiDao.getResepByOpsiId(selectedOpsi.getId()));
        });

        // ── Resep Pilihan section ──
        Label lblResepPilihan = new Label("Resep per Pilihan:");
        lblResepPilihan.setStyle("-fx-font-weight: bold;");

        TableColumn<ResepItem, String> colRPBahan = new TableColumn<>("Bahan");
        colRPBahan.setCellValueFactory(new PropertyValueFactory<>("namaBahan"));
        colRPBahan.setPrefWidth(120);
        TableColumn<ResepItem, Double> colRPJumlah = new TableColumn<>("Jumlah");
        colRPJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahDipakai"));
        colRPJumlah.setPrefWidth(70);
        TableColumn<ResepItem, String> colRPSatuan = new TableColumn<>("Satuan");
        colRPSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colRPSatuan.setPrefWidth(60);
        tableResepPilihan.getColumns().addAll(colRPBahan, colRPJumlah, colRPSatuan);
        tableResepPilihan.setItems(dataResepPilihan);
        tableResepPilihan.setPrefHeight(90);

        cmbBahanResepPilihan.setItems(FXCollections.observableArrayList(bahanDao.getAll()));
        cmbBahanResepPilihan.setPromptText("Pilih bahan...");
        cmbBahanResepPilihan.setMaxWidth(Double.MAX_VALUE);
        txtJumlahResepPilihan.setPromptText("Jumlah per porsi");

        GridPane gridResepPilihan = new GridPane();
        gridResepPilihan.setHgap(8); gridResepPilihan.setVgap(6);
        gridResepPilihan.addRow(0, new Label("Bahan:"), cmbBahanResepPilihan,
                                   new Label("Jumlah:"), txtJumlahResepPilihan);

        Button btnTambahResepPilihan = new Button("✚ Tambah");
        Button btnHapusResepPilihan  = new Button("✖ Hapus");

        btnTambahResepPilihan.setOnAction(e -> {
            if (selectedPilihan == null) { showAlert("Pilih pilihan dari tabel dulu!"); return; }
            Bahan b = cmbBahanResepPilihan.getValue();
            if (b == null) { showAlert("Pilih bahan!"); return; }
            try {
                double jml = Double.parseDouble(txtJumlahResepPilihan.getText().trim());
                if (jml <= 0) { showAlert("Jumlah harus lebih dari 0!"); return; }
                opsiDao.addResepPilihan(selectedPilihan.getId(), b.getId(), jml);
                dataResepPilihan.setAll(opsiDao.getResepByPilihanId(selectedPilihan.getId()));
                cmbBahanResepPilihan.setValue(null);
                txtJumlahResepPilihan.clear();
            } catch (NumberFormatException ex) { showAlert("Jumlah harus angka!"); }
        });

        btnHapusResepPilihan.setOnAction(e -> {
            ResepItem sel = tableResepPilihan.getSelectionModel().getSelectedItem();
            if (sel == null || selectedPilihan == null) { showAlert("Pilih resep dulu!"); return; }
            opsiDao.deleteResepPilihan(selectedPilihan.getId(), sel.getBahanId());
            dataResepPilihan.setAll(opsiDao.getResepByPilihanId(selectedPilihan.getId()));
        });

        // ── Labels & hints ──
        Label lblOpsi    = new Label("Opsi Menu");
        lblOpsi.setStyle("-fx-font-weight: bold;");
        Label lblPilihan = new Label("Pilihan Opsi (tipe 'pilihan')");
        lblPilihan.setStyle("-fx-font-weight: bold;");
        Label hintResepOpsi = new Label("ℹ Pilih opsi checkbox di atas untuk mengelola resepnya.");
        hintResepOpsi.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        Label hintResepPilihan = new Label("ℹ Pilih baris pilihan di atas untuk mengelola resepnya.");
        hintResepPilihan.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        // ── Assemble in ScrollPane ──
        VBox inner = new VBox(8,
            lblOpsi, tableOpsi,
            new Label("Form Opsi:"), gridOpsi,
            new HBox(8, btnTambahOpsi, btnHapusOpsi, btnResetOpsi),
            new Separator(),
            lblPilihan, tablePilihan, gridPilihan,
            new HBox(8, btnTambahPilihan, btnHapusPilihan, btnResetPilihan),
            new Separator(),
            lblResepOpsi, hintResepOpsi, tableResepOpsi, gridResepOpsi,
            new HBox(8, btnTambahResepOpsi, btnHapusResepOpsi),
            new Separator(),
            lblResepPilihan, hintResepPilihan, tableResepPilihan, gridResepPilihan,
            new HBox(8, btnTambahResepPilihan, btnHapusResepPilihan)
        );
        inner.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(inner);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(600);

        return new VBox(scroll);
    }

    // ── DATA LOADERS ─────────────────────────────────────────────
    public void loadData() {
        dataMenu.setAll(menuDao.getAll());
        cmbBahanResep.setItems(FXCollections.observableArrayList(bahanDao.getAll()));
        cmbBahanResepOpsi.setItems(FXCollections.observableArrayList(bahanDao.getAll()));
        cmbBahanResepPilihan.setItems(FXCollections.observableArrayList(bahanDao.getAll()));
    }

    private void loadOpsi(int menuId) {
        dataOpsi.setAll(opsiDao.getByMenuId(menuId));
        dataPilihan.clear();
        dataResepOpsi.clear();
        dataResepPilihan.clear();
        selectedOpsi    = null;
        selectedPilihan = null;
    }

    private void loadPilihan(int opsiId) {
        dataPilihan.setAll(opsiDao.getPilihanByOpsiId(opsiId));
        dataResepPilihan.clear();
        selectedPilihan = null;
        btnTambahPilihan.setText("✚ Tambah Pilihan");
    }

    private void loadResep(int menuId) {
        dataResep.setAll(bahanDao.getResepByMenuId(menuId));
    }

    // ── HELPERS ──────────────────────────────────────────────────
    private MenuItem getMenuFromForm() {
        String nama = txtNamaMenu.getText().trim();
        String tipe = cmbTipe.getValue();
        if (nama.isEmpty()) { showAlert("Nama menu tidak boleh kosong!"); return null; }
        try {
            double harga = Double.parseDouble(txtHargaMenu.getText().trim());
            return new MenuItem(0, nama, harga, tipe);
        } catch (NumberFormatException e) { showAlert("Harga harus berupa angka!"); return null; }
    }

    private void resetMenuForm() {
        txtNamaMenu.clear(); txtHargaMenu.clear(); cmbTipe.setValue("Makanan");
        selectedMenu = null; selectedOpsi = null; selectedPilihan = null;
        dataOpsi.clear(); dataPilihan.clear(); dataResep.clear();
        dataResepOpsi.clear(); dataResepPilihan.clear();
        tableMenu.getSelectionModel().clearSelection();
        if (btnTambahPilihan != null) btnTambahPilihan.setText("✚ Tambah Pilihan");
    }

    private void resetOpsiForm() {
        txtNamaOpsi.clear(); txtHargaOpsi.clear(); cmbTipeOpsi.setValue("pilihan");
        chkWajib.setSelected(false);
        txtHargaOpsi.setVisible(false); txtHargaOpsi.setManaged(false);
        selectedOpsi = null; selectedPilihan = null;
        dataPilihan.clear(); dataResepOpsi.clear(); dataResepPilihan.clear();
        tableOpsi.getSelectionModel().clearSelection();
        txtLabelPilihan.clear(); txtHargaPilihan.clear();
        if (btnTambahPilihan != null) btnTambahPilihan.setText("✚ Tambah Pilihan");
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }
}