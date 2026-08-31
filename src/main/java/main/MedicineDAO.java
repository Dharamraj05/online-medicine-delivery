package main;

import db.DBConnection;
import models.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    private static final String SELECT_ALL =
        "SELECT m.id, m.name, m.brand, c.name AS category, " +
        "m.price, m.stock_quantity, m.description, m.requires_rx " +
        "FROM medicines m JOIN categories c ON m.category_id = c.id";

    public List<Medicine> getAllMedicines() throws SQLException {
        List<Medicine> list = new ArrayList<>();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(SELECT_ALL)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Medicine> searchMedicines(String keyword) throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String sql = SELECT_ALL + " WHERE m.name LIKE ? OR m.brand LIKE ? OR c.name LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Medicine getMedicineById(int id) throws SQLException {
        String sql = SELECT_ALL + " WHERE m.id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void addMedicine(String name, String brand, int categoryId,
                            double price, int stock, String desc, boolean rx) throws SQLException {
        String sql = "INSERT INTO medicines(name,brand,category_id,price,stock_quantity,description,requires_rx) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);   ps.setString(2, brand);
            ps.setInt(3, categoryId); ps.setDouble(4, price);
            ps.setInt(5, stock);     ps.setString(6, desc);
            ps.setBoolean(7, rx);
            ps.executeUpdate();
        }
    }

    public void updateMedicine(int id, double price, int stock) throws SQLException {
        String sql = "UPDATE medicines SET price=?, stock_quantity=? WHERE id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, price); ps.setInt(2, stock); ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void deleteMedicine(int id) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("DELETE FROM medicines WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void reduceStock(int medicineId, int qty) throws SQLException {
        String sql = "UPDATE medicines SET stock_quantity = stock_quantity - ? " +
                     "WHERE id = ? AND stock_quantity >= ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, qty); ps.setInt(2, medicineId); ps.setInt(3, qty);
            int rows = ps.executeUpdate();
            if (rows == 0)
                throw new SQLException("Insufficient stock for medicine ID " + medicineId);
        }
    }

    private Medicine map(ResultSet rs) throws SQLException {
        return new Medicine(
            rs.getInt("id"),           rs.getString("name"),
            rs.getString("brand"),     rs.getString("category"),
            rs.getDouble("price"),     rs.getInt("stock_quantity"),
            rs.getString("description"), rs.getBoolean("requires_rx")
        );
    }
}
