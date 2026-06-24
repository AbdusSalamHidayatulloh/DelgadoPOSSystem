package com.testproject.ui.menu;

import com.testproject.model.MenuItem;
import com.testproject.service.MenuService;
import com.testproject.utils.UIHelper;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MenuLeftPane extends VBox {
    private final MenuService service;
    private final TableView<MenuItem> tableMenu = new TableView<>();
    private final TextField txtNama = new TextField();
    private final TextField txtHarga = new TextField();
    private final ComboBox<String> cmbTipe = new ComboBox<>();
    
    private MenuItem selectedMenu = null;

    private List<MenuItem> allMenuItems = new ArrayList<>();
    private final Pagination pagination = new Pagination();
    private final int ROWS_PER_PAGE = 15;

    public MenuLeftPane(MenuService service, Consumer<MenuItem> onMenuSelected) {
        this.service = service;
        setSpacing(8);
        setPrefWidth(350);

        Label lbl = new Label("Daftar Menu");
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TableColumn<MenuItem, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNama.setPrefWidth(120);

        TableColumn<MenuItem, Double> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colHarga.setCellFactory(col -> new UIHelper.FormatDesimalCell<>());
        colHarga.setPrefWidth(90);

        TableColumn<MenuItem, String> colTipe = new TableColumn<>("Tipe");
        colTipe.setCellValueFactory(new PropertyValueFactory<>("tipe"));
        colTipe.setPrefWidth(80);

        tableMenu.getColumns().addAll(colNama, colHarga, colTipe);
        tableMenu.setPrefHeight(350);

        tableMenu.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            selectedMenu = n;
            if (n != null) {
                txtNama.setText(n.getNama());
                txtHarga.setText(String.valueOf(n.getHarga() == Math.floor(n.getHarga()) ? (int) n.getHarga() : n.getHarga()));
                cmbTipe.setValue(n.getTipe());
            }
            onMenuSelected.accept(n); 
        });

        pagination.setPageFactory(this::createPage);

        txtNama.setPromptText("Nama menu");
        txtHarga.setPromptText("Harga");
        
        cmbTipe.setEditable(true);
        cmbTipe.setPromptText("Pilih atau ketik kategori baru...");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.addRow(0, new Label("Nama:"), txtNama);
        grid.addRow(1, new Label("Harga:"), txtHarga);
        grid.addRow(2, new Label("Tipe:"), cmbTipe);

        Button btnTambah = new Button("Tambah");
        Button btnUpdate = new Button("Update");
        Button btnHapus = new Button("Hapus");
        Button btnReset = new Button("Reset");

        btnTambah.setOnAction(e -> {
            if (selectedMenu != null) { UIHelper.showAlert(Alert.AlertType.WARNING, "Info", "Tekan Reset untuk tambah baru!"); return; }
            if (cmbTipe.getValue() == null || cmbTipe.getValue().trim().isEmpty()) {
                UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Tipe kategori tidak boleh kosong!"); return;
            }
            try {
                service.simpanMenu(null, txtNama.getText(), txtHarga.getText(), cmbTipe.getValue().trim());
                refreshData(); resetForm();
            } catch (IllegalArgumentException ex) { UIHelper.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        });

        btnUpdate.setOnAction(e -> {
            if (selectedMenu == null) return;
            if (cmbTipe.getValue() == null || cmbTipe.getValue().trim().isEmpty()) {
                UIHelper.showAlert(Alert.AlertType.WARNING, "Peringatan", "Tipe kategori tidak boleh kosong!"); return;
            }
            try {
                service.simpanMenu(selectedMenu.getId(), txtNama.getText(), txtHarga.getText(), cmbTipe.getValue().trim());
                refreshData(); resetForm();
            } catch (IllegalArgumentException ex) { UIHelper.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        });

        btnHapus.setOnAction(e -> {
            if (selectedMenu == null) return;
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus menu '" + selectedMenu.getNama() + "'?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    try {
                        service.hapusMenu(selectedMenu.getId());
                        refreshData(); 
                        resetForm();
                        UIHelper.showAlert(Alert.AlertType.INFORMATION, "Sukses", "Menu berhasil dihapus.");
                    } catch (IllegalStateException ex) {
                        // Menangkap pelemparan error "Menu Terpakai" dari Service
                        UIHelper.showAlert(Alert.AlertType.ERROR, "Penolakan Sistem", ex.getMessage());
                    } catch (Exception ex) {
                        UIHelper.showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan: " + ex.getMessage());
                    }
                }
            });
        });

        btnReset.setOnAction(e -> resetForm());

        HBox buttons = new HBox(8, btnTambah, btnUpdate, btnHapus, btnReset);
        getChildren().addAll(lbl, pagination, new Label("Form Menu:"), grid, buttons);
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allMenuItems.size());
        
        if (fromIndex <= toIndex && !allMenuItems.isEmpty()) {
            tableMenu.getItems().setAll(allMenuItems.subList(fromIndex, toIndex));
        } else {
            tableMenu.getItems().clear();
        }
        return tableMenu;
    }

    public void refreshData() {
        allMenuItems = service.ambilSemuaMenu().stream()
                .filter(m -> !(m.getNama().equalsIgnoreCase("Item Manual") && m.getTipe().equalsIgnoreCase("Kustom")))
                .collect(Collectors.toList());

        List<String> categories = allMenuItems.stream()
                .map(MenuItem::getTipe)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (categories.isEmpty()) {
            categories.add("Makanan");
            categories.add("Minuman");
        }

        String currentVal = cmbTipe.getValue();
        cmbTipe.getItems().setAll(categories);
        if (currentVal != null && !currentVal.isEmpty()) cmbTipe.setValue(currentVal);
        else cmbTipe.setValue(categories.get(0));

        int pageCount = (int) Math.ceil((double) allMenuItems.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        
        int currPage = pagination.getCurrentPageIndex();
        if (currPage >= pageCount && pageCount > 0) currPage = pageCount - 1;
        
        pagination.setCurrentPageIndex(currPage);
        createPage(currPage);
    }

    private void resetForm() {
        txtNama.clear(); 
        txtHarga.clear(); 
        if (!cmbTipe.getItems().isEmpty()) cmbTipe.setValue(cmbTipe.getItems().get(0));
        else cmbTipe.setValue("Makanan");
        selectedMenu = null;
        tableMenu.getSelectionModel().clearSelection();
    }
}