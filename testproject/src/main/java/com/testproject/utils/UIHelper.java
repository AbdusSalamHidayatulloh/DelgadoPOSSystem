package com.testproject.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;

public class UIHelper {

    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Class untuk menghilangkan ".0" pada angka desimal di tabel
    public static class FormatDesimalCell<S> extends TableCell<S, Double> {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                if (item == Math.floor(item)) {
                    setText(String.valueOf(item.intValue()));
                } else {
                    setText(String.valueOf(item));
                }
            }
        }
    }
}