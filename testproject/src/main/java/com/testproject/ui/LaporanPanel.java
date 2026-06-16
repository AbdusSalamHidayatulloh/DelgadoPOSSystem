package com.testproject.ui;

import com.testproject.db.DatabaseHelper;
import com.testproject.model.RiwayatRestock;
import com.testproject.model.StatusPembayaran;
import com.testproject.model.Transaksi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;
import java.time.LocalDate;

public class LaporanPanel extends VBox {

    private final TableView<RiwayatRestock> tableRestock = new TableView<>();
    private final ObservableList<RiwayatRestock> dataRestock = FXCollections.observableArrayList();

    private final TableView<Transaksi> tableTransaksi = new TableView<>();
    private final ObservableList<Transaksi> dataTransaksi = FXCollections.observableArrayList();

    private final DatePicker dpDari = new DatePicker(LocalDate.now().minusMonths(1));
    private final DatePicker dpSampai = new DatePicker(LocalDate.now());

    private Label lblRekapPenghasilan;
    private Label lblRekapPengeluaran;
    private Label lblRekapProfit;

    private TabPane tabPane;

    public LaporanPanel() {
        setPadding(new Insets(20));
        setSpacing(15);

        Label title = new Label("Laporan");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabRestock = new Tab("📦 Riwayat Restock", buildRestockTab());
        Tab tabTransaksi = new Tab("🧾 Riwayat Transaksi", buildTransaksiTab());
        Tab tabRekap = new Tab("📊 Rekap", buildRekapTab());

        tabPane.getTabs().addAll(tabRestock, tabTransaksi, tabRekap);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabRestock)
                loadRestock();
            else if (newTab == tabTransaksi)
                loadTransaksi();
            else if (newTab == tabRekap)
                loadRekapValues();
        });

        getChildren().addAll(title, tabPane);
        loadRestock();
    }

    public void loadData() {
        int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
        if (selectedIndex == 0)
            loadRestock();
        else if (selectedIndex == 1)
            loadTransaksi();
        else if (selectedIndex == 2)
            loadRekapValues();
    }

    private VBox buildRestockTab() {
        HBox filter = new HBox(10,
                new Label("Dari:"), dpDari,
                new Label("Sampai:"), dpSampai,
                buildFilterButton(this::loadRestock));
        filter.setPadding(new Insets(10, 0, 10, 0));

        TableColumn<RiwayatRestock, String> colTanggal = new TableColumn<>("Tanggal");
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colTanggal.setPrefWidth(100);

        TableColumn<RiwayatRestock, Integer> colBahan = new TableColumn<>("Bahan");
        colBahan.setCellValueFactory(new PropertyValueFactory<>("bahanId"));
        colBahan.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                setText(empty || id == null ? null : getNamaBahan(id));
            }
        });
        colBahan.setPrefWidth(130);

        TableColumn<RiwayatRestock, Double> colJumlah = new TableColumn<>("Jumlah");
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahTambah"));
        colJumlah.setPrefWidth(80);

        TableColumn<RiwayatRestock, Double> colHargaTotal = new TableColumn<>("Harga Total");
        colHargaTotal.setCellValueFactory(new PropertyValueFactory<>("hargaTotal"));
        colHargaTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %.0f", val));
            }
        });
        colHargaTotal.setPrefWidth(120);

        TableColumn<RiwayatRestock, Double> colHargaSatuan = new TableColumn<>("Harga/Satuan");
        colHargaSatuan.setCellValueFactory(new PropertyValueFactory<>("hargaPerSatuan"));
        colHargaSatuan.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %.0f", val));
            }
        });
        colHargaSatuan.setPrefWidth(120);

        tableRestock.getColumns().addAll(colTanggal, colBahan, colJumlah, colHargaTotal, colHargaSatuan);
        tableRestock.setItems(dataRestock);

        Label lblTotal = new Label("Total Pengeluaran: Rp 0");
        lblTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        dataRestock.addListener((javafx.collections.ListChangeListener<RiwayatRestock>) c -> {
            double total = dataRestock.stream().mapToDouble(RiwayatRestock::getHargaTotal).sum();
            lblTotal.setText(String.format("Total Pengeluaran: Rp %.0f", total));
        });

        VBox box = new VBox(10, filter, tableRestock, lblTotal);
        box.setPadding(new Insets(10));
        return box;
    }

    private VBox buildTransaksiTab() {
        HBox filter = new HBox(10,
                new Label("Dari:"), dpDari,
                new Label("Sampai:"), dpSampai,
                buildFilterButton(this::loadTransaksi));
        filter.setPadding(new Insets(10, 0, 10, 0));

        TableColumn<Transaksi, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Transaksi, String> colTanggal = new TableColumn<>("Tanggal");
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colTanggal.setPrefWidth(120);

        TableColumn<Transaksi, String> colNama = new TableColumn<>("Pelanggan");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan"));
        colNama.setPrefWidth(120);

        TableColumn<Transaksi, Double> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %.0f", val));
            }
        });
        colTotal.setPrefWidth(120);

        TableColumn<Transaksi, String> colMetode = new TableColumn<>("Metode");
        colMetode.setCellValueFactory(new PropertyValueFactory<>("metodeBayar"));
        colMetode.setPrefWidth(100);

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

        Label lblTotal = new Label("Total Penghasilan: Rp 0");
        lblTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        dataTransaksi.addListener((javafx.collections.ListChangeListener<Transaksi>) c -> {
            double total = dataTransaksi.stream().mapToDouble(Transaksi::getTotalHarga).sum();
            lblTotal.setText(String.format("Total Penghasilan: Rp %.0f", total));
        });

        VBox box = new VBox(10, filter, tableTransaksi, lblTotal);
        box.setPadding(new Insets(10));
        return box;
    }

    private VBox buildRekapTab() {
        lblRekapPenghasilan = new Label("Total Penghasilan: Rp 0");
        lblRekapPengeluaran = new Label("Total Pengeluaran: Rp 0");
        lblRekapProfit = new Label("Estimasi Profit: Rp 0");

        lblRekapPenghasilan.setStyle("-fx-font-size: 14px;");
        lblRekapPengeluaran.setStyle("-fx-font-size: 14px;");
        lblRekapProfit.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox filter = new HBox(10,
                new Label("Dari:"), dpDari,
                new Label("Sampai:"), dpSampai,
                buildFilterButton(this::loadRekapValues));
        filter.setPadding(new Insets(10, 0, 10, 0));

        VBox box = new VBox(15, filter, new Separator(),
                lblRekapPenghasilan, lblRekapPengeluaran,
                new Separator(), lblRekapProfit);
        box.setPadding(new Insets(20));
        return box;
    }

    private void loadRestock() {
        String dari = dpDari.getValue().toString();
        String sampai = dpSampai.getValue().toString();
        dataRestock.clear();
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM riwayat_restock WHERE tanggal BETWEEN ? AND ? ORDER BY tanggal DESC")) {
            stmt.setString(1, dari);
            stmt.setString(2, sampai);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dataRestock.add(new RiwayatRestock(
                        rs.getInt("id"),
                        rs.getInt("bahan_id"),
                        rs.getString("tanggal"),
                        rs.getDouble("jumlah_tambah"),
                        rs.getDouble("harga_total"),
                        rs.getDouble("harga_per_satuan")));
            }
        } catch (Exception e) {
            System.out.println("Error loadRestock: " + e.getMessage());
        }
    }

    private void loadTransaksi() {
        String dari = dpDari.getValue().toString();
        String sampai = dpSampai.getValue().toString();
        dataTransaksi.clear();
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM transaksi WHERE tanggal BETWEEN ? AND ? ORDER BY tanggal DESC")) {
            stmt.setString(1, dari);
            stmt.setString(2, sampai);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                dataTransaksi.add(new Transaksi(
                        rs.getInt("id"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("tanggal"),
                        rs.getDouble("total_harga"),
                        rs.getString("metode_bayar"),
                        com.testproject.model.StatusPembayaran.valueOf(rs.getString("status_pembayaran"))));
            }
        } catch (Exception e) {
            System.out.println("Error loadTransaksi: " + e.getMessage());
        }
    }

    private void loadRekapValues() {
        String dari = dpDari.getValue().toString();
        String sampai = dpSampai.getValue().toString();

        double penghasilan = 0;
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT SUM(total_harga) FROM transaksi WHERE tanggal BETWEEN ? AND ? AND status_pembayaran = 'LUNAS'")) {
            stmt.setString(1, dari);
            stmt.setString(2, sampai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                penghasilan = rs.getDouble(1);
        } catch (Exception e) {
            System.out.println("Error hitung penghasilan: " + e.getMessage());
        }

        double pengeluaran = 0;
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT SUM(harga_total) FROM riwayat_restock WHERE tanggal BETWEEN ? AND ?")) {
            stmt.setString(1, dari);
            stmt.setString(2, sampai);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                pengeluaran = rs.getDouble(1);
        } catch (Exception e) {
            System.out.println("Error hitung pengeluaran: " + e.getMessage());
        }

        double profit = penghasilan - pengeluaran;
        lblRekapPenghasilan.setText(String.format("Total Penghasilan: Rp %.0f", penghasilan));
        lblRekapPengeluaran.setText(String.format("Total Pengeluaran Bahan: Rp %.0f", pengeluaran));
        lblRekapProfit.setText(String.format("Estimasi Profit: Rp %.0f", profit));
        lblRekapProfit.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: "
                + (profit >= 0 ? "green" : "red") + ";");
    }

    private Button buildFilterButton(Runnable action) {
        Button btn = new Button("🔍 Filter");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private String getNamaBahan(int id) {
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT nama FROM bahan WHERE id=?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getString("nama");
        } catch (Exception e) {
            System.out.println("Error getNamaBahan: " + e.getMessage());
        }
        return "Unknown";
    }
}