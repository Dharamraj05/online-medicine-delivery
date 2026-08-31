package main;

import db.DBConnection;
import models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public Customer login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM customers WHERE email=? AND password=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email); ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public boolean register(String fullName, String email, String phone,
                            String address, String password) throws SQLException {
        String sql = "INSERT INTO customers(full_name,email,phone,address,password) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, fullName); ps.setString(2, email);
            ps.setString(3, phone);   ps.setString(4, address);
            ps.setString(5, password);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM customers")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Customer getById(int id) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("SELECT * FROM customers WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public boolean updateProfile(int id, String phone, String address) throws SQLException {
        String sql = "UPDATE customers SET phone=?, address=? WHERE id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, phone); ps.setString(2, address); ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("id"),           rs.getString("full_name"),
            rs.getString("email"),     rs.getString("phone"),
            rs.getString("address"),   rs.getString("password")
        );
    }
}
