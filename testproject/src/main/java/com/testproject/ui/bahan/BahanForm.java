package com.testproject.ui.bahan;

import com.testproject.service.BahanService;
import com.testproject.utils.UIHelper;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class BahanForm extends GridPane {
    private final TextField txtNama = new TextField();
    private final TextField txtStok = new TextField();
    private final TextField txtStokMin = new TextField();
    private final TextField txtSatuan = new TextField();

    public BahanForm(BahanService service, Runnable onSuksesTambah) {
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(10, 0, 10, 0));

        txtNama.setPromptText("Contoh: Susu Cair");
        txtStok.setPromptText("Contoh: 10");
        txtStokMin.setPromptText("Contoh: 2");
        txtSatuan.setPromptText("Contoh: Liter, Gram, Pcs");

        add(new Label("Nama Bahan:"), 0, 0); add(txtNama, 1, 0);
        add(new Label("Stok Awal:"), 0, 1); add(txtStok, 1, 1);
        add(new Label("Stok Minimum:"), 0, 2); add(txtStokMin, 1, 2);
        add(new Label("Satuan:"), 0, 3); add(txtSatuan, 1, 3);

        Button btnTambah = new Button("Tambah Bahan");
        btnTambah.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        add(btnTambah, 1, 4);

        btnTambah.setOnAction(e -> {
            try {
                boolean sukses = service.tambahBahanBaru(
                        txtNama.getText(), txtStok.getText(), txtStokMin.getText(), txtSatuan.getText()
                );
                if (sukses) {
                    UIHelper.showAlert(Alert.AlertType.INFORMATION, "Sukses", "Bahan baku berhasil ditambahkan!");
                    txtNama.clear(); txtStok.clear(); txtStokMin.clear(); txtSatuan.clear();
                    onSuksesTambah.run();
                }
            } catch (IllegalArgumentException ex) {
                UIHelper.showAlert(Alert.AlertType.WARNING, "Validasi Gagal", ex.getMessage());
            }
        });
    }
}