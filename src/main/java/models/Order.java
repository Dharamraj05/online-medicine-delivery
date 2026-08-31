package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id, customerId;
    private double totalAmount;
    private String deliveryAddr, status, paymentMethod, customerName;
    private Timestamp placedAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    // ── Inner class ───────────────────────────────────────────
    public static class OrderItem {
        public int    medicineId, quantity;
        public double unitPrice;
        public String medicineName;

        public OrderItem(int medicineId, String medicineName,
                         int quantity, double unitPrice) {
            this.medicineId   = medicineId;
            this.medicineName = medicineName;
            this.quantity     = quantity;
            this.unitPrice    = unitPrice;
        }

        public double getSubtotal() { return quantity * unitPrice; }
    }

    // ── Getters ───────────────────────────────────────────────
    public int               getId()            { return id; }
    public int               getCustomerId()    { return customerId; }
    public double            getTotalAmount()   { return totalAmount; }
    public String            getDeliveryAddr()  { return deliveryAddr; }
    public String            getStatus()        { return status; }
    public String            getPaymentMethod() { return paymentMethod; }
    public String            getCustomerName()  { return customerName; }
    public Timestamp         getPlacedAt()      { return placedAt; }
    public List<OrderItem>   getItems()         { return items; }

    // ── Setters ───────────────────────────────────────────────
    public void setId(int id)                   { this.id = id; }
    public void setCustomerId(int c)            { this.customerId = c; }
    public void setTotalAmount(double t)        { this.totalAmount = t; }
    public void setDeliveryAddr(String a)       { this.deliveryAddr = a; }
    public void setStatus(String s)             { this.status = s; }
    public void setPaymentMethod(String p)      { this.paymentMethod = p; }
    public void setCustomerName(String n)       { this.customerName = n; }
    public void setPlacedAt(Timestamp t)        { this.placedAt = t; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public void addItem(OrderItem item)         { this.items.add(item); }
}
