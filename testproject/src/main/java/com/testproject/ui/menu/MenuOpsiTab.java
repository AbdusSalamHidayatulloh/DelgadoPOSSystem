package com.testproject.ui.menu;

// Import Model secara spesifik agar tidak bentrok dengan JavaFX
import com.testproject.model.MenuItem; 
import com.testproject.model.OpsiMenu;
import com.testproject.model.PilihanOpsi;
import com.testproject.service.MenuService;
import com.testproject.utils.UIHelper;

// Import JavaFX secara spesifik (Tanpa bintang *)
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MenuOpsiTab extends ScrollPane {
    private final MenuService service;
    private MenuItem currentMenu;
    private OpsiMenu selectedOpsi;

    private final TableView<OpsiMenu> tableOpsi = new TableView<>();
    private final TableView<PilihanOpsi> tablePilihan = new TableView<>();

    public MenuOpsiTab(MenuService service) {
        this.service = service;
        setFitToWidth(true);
        setPrefHeight(600);

        VBox inner = new VBox(15);
        inner.setPadding(new Insets(10));

        inner.getChildren().addAll(
            buildOpsiSection(),
            new Separator(),
            buildPilihanSection()
        );
        setContent(inner);
    }

    public void setMenu(MenuItem menu) {
        this.currentMenu = menu;
        refreshOpsi();
    }

    private VBox buildOpsiSection() {
        Label lbl = new Label("1. Opsi Menu (Misal: Topping, Level Pedas)");
        lbl.setStyle("-fx-font-weight: bold;");

        TableColumn<OpsiMenu, String> colNama = new TableColumn<>("Nama Opsi"); colNama.setCellValueFactory(new PropertyValueFactory<>("namaOpsi")); colNama.setPrefWidth(120);
        TableColumn<OpsiMenu, String> colTipe = new TableColumn<>("Tipe"); colTipe.setCellValueFactory(new PropertyValueFactory<>("tipeOpsi"));
        TableColumn<OpsiMenu, Double> colHarga = new TableColumn<>("Harga Tambahan"); colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaTambahan")); colHarga.setCellFactory(c -> new UIHelper.FormatDesimalCell<>());
        
        tableOpsi.getColumns().addAll(colNama, colTipe, colHarga);
        tableOpsi.setPrefHeight(120);

        TextField txtNamaOpsi = new TextField(); txtNamaOpsi.setPromptText("Nama Opsi");
        ComboBox<String> cmbTipe = new ComboBox<>(); cmbTipe.getItems().addAll("checkbox", "pilihan"); cmbTipe.setValue("pilihan");
        TextField txtHarga = new TextField(); txtHarga.setPromptText("Harga (Cuma checkbox)");

        GridPane grid = new GridPane(); grid.setHgap(8); grid.setVgap(8);
        grid.addRow(0, new Label("Nama:"), txtNamaOpsi, new Label("Tipe:"), cmbTipe);
        grid.addRow(1, new Label("Harga:"), txtHarga);

        tableOpsi.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            selectedOpsi = n;
            if (n != null) {
                txtNamaOpsi.setText(n.getNamaOpsi());
                cmbTipe.setValue(n.getTipeOpsi());
                txtHarga.setText(String.valueOf(n.getHargaTambahan()));
            }
            refreshPilihan();
        });

        Button btnTambah = new Button("Tambah Opsi");
        btnTambah.setOnAction(e -> {
            if (currentMenu == null) return;
            try {
                service.tambahOpsi(currentMenu.getId(), txtNamaOpsi.getText(), cmbTipe.getValue(), false, txtHarga.getText());
                refreshOpsi(); txtNamaOpsi.clear(); txtHarga.clear();
            } catch (IllegalArgumentException ex) { UIHelper.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        });

        Button btnHapus = new Button("Hapus Opsi");
        btnHapus.setOnAction(e -> { if(selectedOpsi != null) { service.hapusOpsi(selectedOpsi.getId()); refreshOpsi(); } });

        return new VBox(8, lbl, tableOpsi, grid, new HBox(8, btnTambah, btnHapus));
    }

    private VBox buildPilihanSection() {
        Label lbl = new Label("2. Pilihan Opsi (Hanya jika tipe opsi = 'pilihan')");
        lbl.setStyle("-fx-font-weight: bold;");

        TableColumn<PilihanOpsi, String> colLabel = new TableColumn<>("Label"); colLabel.setCellValueFactory(new PropertyValueFactory<>("label")); colLabel.setPrefWidth(120);
        TableColumn<PilihanOpsi, Double> colHarga = new TableColumn<>("Harga Tambahan"); colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaTambahan")); colHarga.setCellFactory(c -> new UIHelper.FormatDesimalCell<>());
        
        tablePilihan.getColumns().addAll(colLabel, colHarga);
        tablePilihan.setPrefHeight(120);

        TextField txtLabel = new TextField(); txtLabel.setPromptText("Contoh: Pedas, Manis");
        TextField txtHarga = new TextField(); txtHarga.setPromptText("Harga");

        GridPane grid = new GridPane(); grid.setHgap(8); grid.setVgap(8);
        grid.addRow(0, new Label("Label:"), txtLabel, new Label("Harga:"), txtHarga);

        Button btnTambah = new Button("Tambah Pilihan");
        btnTambah.setOnAction(e -> {
            if (selectedOpsi == null || selectedOpsi.getTipeOpsi().equals("checkbox")) return;
            try {
                service.simpanPilihan(null, selectedOpsi.getId(), txtLabel.getText(), txtHarga.getText());
                refreshPilihan(); txtLabel.clear(); txtHarga.clear();
            } catch (IllegalArgumentException ex) { UIHelper.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        });

        Button btnHapus = new Button("Hapus Pilihan");
        btnHapus.setOnAction(e -> {
            PilihanOpsi sel = tablePilihan.getSelectionModel().getSelectedItem();
            if (sel != null) { service.hapusPilihan(sel.getId()); refreshPilihan(); }
        });

        return new VBox(8, lbl, tablePilihan, grid, new HBox(8, btnTambah, btnHapus));
    }

    private void refreshOpsi() {
        if (currentMenu != null) tableOpsi.getItems().setAll(service.ambilOpsiByMenu(currentMenu.getId()));
        else tableOpsi.getItems().clear();
        refreshPilihan();
    }

    private void refreshPilihan() {
        if (selectedOpsi != null) tablePilihan.getItems().setAll(service.ambilPilihanByOpsi(selectedOpsi.getId()));
        else tablePilihan.getItems().clear();
    }
}