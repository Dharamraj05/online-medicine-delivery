package main;

import db.DBConnection;
import models.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public int placeOrder(int customerId, double total, String deliveryAddr,
                          String paymentMethod, List<Order.OrderItem> items) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            // Insert order header
            String sql = "INSERT INTO orders(customer_id,total_amount,delivery_addr,payment_method) VALUES(?,?,?,?)";
            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, customerId); ps.setDouble(2, total);
                ps.setString(3, deliveryAddr); ps.setString(4, paymentMethod);
                ps.executeUpdate();
                ResultSet gk = ps.getGeneratedKeys();
                gk.next();
                orderId = gk.getInt(1);
            }

            // Insert line items & reduce stock
            MedicineDAO medDAO = new MedicineDAO();
            String itemSql = "INSERT INTO order_items(order_id,medicine_id,quantity,unit_price) VALUES(?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (Order.OrderItem item : items) {
                    ps.setInt(1, orderId);       ps.setInt(2, item.medicineId);
                    ps.setInt(3, item.quantity); ps.setDouble(4, item.unitPrice);
                    ps.addBatch();
                    medDAO.reduceStock(item.medicineId, item.quantity);
                }
                ps.executeBatch();
            }

            conn.commit();
            return orderId;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<Order> getOrdersByCustomer(int customerId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.full_name FROM orders o " +
                     "JOIN customers c ON o.customer_id=c.id " +
                     "WHERE o.customer_id=? ORDER BY o.placed_at DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapOrder(rs));
            }
        }
        return list;
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.full_name FROM orders o " +
                     "JOIN customers c ON o.customer_id=c.id ORDER BY o.placed_at DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapOrder(rs));
        }
        return list;
    }

    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement("UPDATE orders SET status=? WHERE id=?")) {
            ps.setString(1, status); ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setDeliveryAddr(rs.getString("delivery_addr"));
        o.setStatus(rs.getString("status"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setCustomerName(rs.getString("full_name"));
        o.setPlacedAt(rs.getTimestamp("placed_at"));
        return o;
    }
}
