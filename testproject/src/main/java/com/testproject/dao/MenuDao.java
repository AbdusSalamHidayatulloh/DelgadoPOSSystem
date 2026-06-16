package com.testproject.dao;

import com.testproject.db.DatabaseHelper;
import com.testproject.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDao {

    public List<MenuItem> getAll() {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM menu";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new MenuItem(
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getDouble("harga"),
                    rs.getString("tipe")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error getAll MenuItem: " + e.getMessage());
        }
        return list;
    }

    public void add(MenuItem m) {
        String sql = "INSERT INTO menu (nama, harga, tipe) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getNama());
            stmt.setDouble(2, m.getHarga());
            stmt.setString(3, m.getTipe());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error add MenuItem: " + e.getMessage());
        }
    }

    public void update(MenuItem m) {
        String sql = "UPDATE menu SET nama=?, harga=?, tipe=? WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getNama());
            stmt.setDouble(2, m.getHarga());
            stmt.setString(3, m.getTipe());
            stmt.setInt(4, m.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error update MenuItem: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM menu WHERE id=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error delete MenuItem: " + e.getMessage());
        }
    }
}