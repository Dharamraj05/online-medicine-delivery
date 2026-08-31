package ui;

import db.DBConnection;
import main.CustomerDAO;
import main.MedicineDAO;
import main.OrderDAO;
import models.Customer;
import models.Medicine;
import models.Order;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;

import java.sql.SQLException;
import java.util.*;

public class MediSwiftApp extends Application {

    // ── Colors ────────────────────────────────────────────────
    private static final String C_PRIMARY   = "#0EA5E9";
    private static final String C_SUCCESS   = "#10B981";
    private static final String C_ACCENT    = "#F59E0B";
    private static final String C_DANGER    = "#EF4444";
    private static final String C_BG        = "#F0F9FF";
    private static final String C_TEXT      = "#0F172A";
    private static final String C_MUTED     = "#64748B";

    private static final String BTN_PRIMARY =
        "-fx-background-color:#0EA5E9;-fx-text-fill:white;-fx-font-weight:bold;" +
        "-fx-background-radius:8;-fx-cursor:hand;-fx-padding:8 20;-fx-font-size:13;";
    private static final String BTN_SUCCESS =
        "-fx-background-color:#10B981;-fx-text-fill:white;-fx-font-weight:bold;" +
        "-fx-background-radius:8;-fx-cursor:hand;-fx-padding:8 20;-fx-font-size:13;";
    private static final String BTN_DANGER =
        "-fx-background-color:#EF4444;-fx-text-fill:white;-fx-font-weight:bold;" +
        "-fx-background-radius:8;-fx-cursor:hand;-fx-padding:6 14;-fx-font-size:12;";
    private static final String FIELD_STYLE =
        "-fx-background-color:white;-fx-border-color:#CBD5E1;-fx-border-radius:6;" +
        "-fx-background-radius:6;-fx-padding:8;-fx-font-size:13;";

    // ── State ─────────────────────────────────────────────────
    private Stage primaryStage;
    private Customer loggedInCustomer;
    private final Map<Medicine, Integer> cart = new LinkedHashMap<>();

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final MedicineDAO medicineDAO  = new MedicineDAO();
    private final OrderDAO    orderDAO     = new OrderDAO();

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("💊 MediSwift — Online Medicine Delivery");
        stage.setMinWidth(950);
        stage.setMinHeight(650);
        showLoginScreen();
        stage.show();
    }

    // ════════════════════════════════════════════════════════
    // LOGIN
    // ════════════════════════════════════════════════════════
    private void showLoginScreen() {
        // Left hero
        VBox hero = new VBox(20);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(40));
        hero.setMinWidth(300);
        hero.setStyle("-fx-background-color:linear-gradient(to bottom,#0EA5E9,#10B981);");

        Label ico  = new Label("💊");
        ico.setFont(Font.font("Arial", 60));

        Label name = new Label("MediSwift");
        name.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        name.setTextFill(Color.WHITE);

        Label tag = new Label("Your health, delivered fast.\nMedicines at your doorstep.");
        tag.setTextFill(Color.web("#E0F2FE"));
        tag.setFont(Font.font("Arial", 14));
        tag.setTextAlignment(TextAlignment.CENTER);

        hero.getChildren().addAll(ico, name, tag);

        // Right form
        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(50));
        form.setStyle("-fx-background-color:" + C_BG + ";");

        Label title = label("Welcome Back!", 26, true, C_TEXT);
        Label sub   = label("Login to your MediSwift account", 13, false, C_MUTED);

        TextField     emailF = field("Email Address");
        PasswordField passF  = pass("Password");
        Label         errL   = label("", 12, false, C_DANGER);

        Button loginBtn = btn("🔐  Login", BTN_PRIMARY);
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Button regBtn = linkBtn("New user? Register here");
        Button admBtn = linkBtn("🛠  Admin Panel (password: admin123)");
        admBtn.setStyle(admBtn.getStyle() + "-fx-font-size:11;");

        loginBtn.setOnAction(e -> {
            String em = emailF.getText().trim(), pw = passF.getText();
            if (em.isEmpty() || pw.isEmpty()) { errL.setText("Please fill all fields."); return; }
            try {
                Customer c = customerDAO.login(em, pw);
                if (c != null) { loggedInCustomer = c; showHome(); }
                else errL.setText("❌ Invalid email or password.");
            } catch (SQLException ex) { errL.setText("DB Error: " + ex.getMessage()); }
        });
        regBtn.setOnAction(e -> showRegister());
        admBtn.setOnAction(e -> showAdminLogin());

        form.getChildren().addAll(title, sub, emailF, passF, errL, loginBtn, regBtn, admBtn);
        HBox root = new HBox(hero, form);
        HBox.setHgrow(form, Priority.ALWAYS);
        primaryStage.setScene(new Scene(root, 800, 520));
    }

    private void showAdminLogin() {
        Stage s = new Stage();
        s.setTitle("Admin Login");
        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER); form.setPadding(new Insets(40));
        form.setStyle("-fx-background-color:" + C_BG + ";");

        Label title = label("🛠 Admin Panel", 22, true, C_TEXT);
        PasswordField passF = pass("Admin Password (admin123)");
        Label errL = label("", 12, false, C_DANGER);
        Button loginBtn = btn("Enter Admin Panel", BTN_PRIMARY);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            if (passF.getText().equals("admin123")) { s.close(); showAdminPanel(); }
            else errL.setText("❌ Wrong password.");
        });
        form.getChildren().addAll(title, passF, errL, loginBtn);
        s.setScene(new Scene(form, 380, 240));
        s.show();
    }

    // ════════════════════════════════════════════════════════
    // REGISTER
    // ════════════════════════════════════════════════════════
    private void showRegister() {
        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER); form.setPadding(new Insets(40));
        form.setStyle("-fx-background-color:" + C_BG + ";");
        form.setMaxWidth(480);

        Label title = label("Create Account", 24, true, C_TEXT);
        TextField     nameF  = field("Full Name");
        TextField     emailF = field("Email Address");
        TextField     phoneF = field("Phone Number");
        TextArea      addrF  = new TextArea();
        addrF.setPromptText("Full Address"); addrF.setPrefRowCount(2); addrF.setStyle(FIELD_STYLE);
        PasswordField passF  = pass("Password");
        PasswordField pass2F = pass("Confirm Password");
        Label errL = label("", 12, false, C_DANGER);

        Button regBtn  = btn("🚀  Create Account", BTN_SUCCESS);
        Button backBtn = linkBtn("← Back to Login");
        regBtn.setMaxWidth(Double.MAX_VALUE);

        regBtn.setOnAction(e -> {
            if (nameF.getText().isEmpty() || emailF.getText().isEmpty() ||
                phoneF.getText().isEmpty() || addrF.getText().isEmpty() || passF.getText().isEmpty()) {
                errL.setText("Please fill all fields."); return;
            }
            if (!passF.getText().equals(pass2F.getText())) {
                errL.setText("Passwords do not match."); return;
            }
            try {
                boolean ok = customerDAO.register(nameF.getText().trim(), emailF.getText().trim(),
                    phoneF.getText().trim(), addrF.getText().trim(), passF.getText());
                if (ok) { alert("✅ Registration successful! Please login.", Alert.AlertType.INFORMATION); showLoginScreen(); }
                else errL.setText("Email may already exist.");
            } catch (SQLException ex) { errL.setText("Error: " + ex.getMessage()); }
        });
        backBtn.setOnAction(e -> showLoginScreen());

        form.getChildren().addAll(title, nameF, emailF, phoneF, addrF, passF, pass2F, errL, regBtn, backBtn);
        ScrollPane sp = new ScrollPane(new StackPane(form));
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:" + C_BG + ";");
        primaryStage.setScene(new Scene(sp, 600, 600));
    }

    // ════════════════════════════════════════════════════════
    // HOME — Medicine Catalog
    // ════════════════════════════════════════════════════════
    private void showHome() {
        BorderPane root = new BorderPane();
        root.setTop(buildNav());
        root.setCenter(buildCatalog());
        primaryStage.setScene(new Scene(root, 1100, 700));
    }

    private HBox buildNav() {
        HBox nav = new HBox(10);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(12, 20, 12, 20));
        nav.setStyle("-fx-background-color:linear-gradient(to right,#0EA5E9,#10B981);");

        Label logo = new Label("💊 MediSwift");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        logo.setTextFill(Color.WHITE);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button meds    = navBtn("🏠 Medicines");
        Button orders  = navBtn("📦 My Orders");
        Button cartBtn = navBtn("🛒 Cart (" + cart.size() + ")");
        Button prof    = navBtn("👤 " + loggedInCustomer.getFullName());
        Button logout  = navBtn("🚪 Logout");

        meds.setOnAction(e   -> showHome());
        orders.setOnAction(e -> showOrders());
        cartBtn.setOnAction(e-> showCart());
        prof.setOnAction(e   -> showProfile());
        logout.setOnAction(e -> { loggedInCustomer = null; cart.clear(); showLoginScreen(); });

        nav.getChildren().addAll(logo, sp, meds, orders, cartBtn, prof, logout);
        return nav;
    }

    private VBox buildCatalog() {
        VBox view = new VBox(16);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color:" + C_BG + ";");

        // Search
        HBox searchRow = new HBox(10);
        TextField searchF = field("🔍 Search medicines, brands, categories...");
        searchF.setPrefWidth(380);
        Button searchBtn = btn("Search", BTN_PRIMARY);
        Button clearBtn  = btn("Clear", BTN_SUCCESS);
        searchRow.getChildren().addAll(searchF, searchBtn, clearBtn);

        FlowPane grid = new FlowPane();
        grid.setHgap(14); grid.setVgap(14);

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        VBox.setVgrow(sp, Priority.ALWAYS);

        Runnable load = () -> {
            try {
                List<Medicine> meds = searchF.getText().trim().isEmpty()
                    ? medicineDAO.getAllMedicines()
                    : medicineDAO.searchMedicines(searchF.getText().trim());
                grid.getChildren().clear();
                for (Medicine m : meds) grid.getChildren().add(buildMedCard(m));
            } catch (SQLException ex) { alert("DB Error: " + ex.getMessage(), Alert.AlertType.ERROR); }
        };

        load.run();
        searchBtn.setOnAction(e -> load.run());
        clearBtn.setOnAction(e -> { searchF.clear(); load.run(); });
        searchF.setOnAction(e -> load.run());

        view.getChildren().addAll(searchRow, sp);
        return view;
    }

    private VBox buildMedCard(Medicine m) {
        VBox card = new VBox(8);
        card.setPrefWidth(210); card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
            "-fx-border-color:#E2E8F0;-fx-border-radius:12;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");

        Label catBadge = new Label(m.getCategoryName());
        catBadge.setStyle("-fx-background-color:#EFF6FF;-fx-text-fill:#1D4ED8;" +
            "-fx-font-size:11;-fx-padding:2 8;-fx-background-radius:20;-fx-font-weight:bold;");

        Label nameL  = label(m.getName(), 13, true, C_TEXT);
        nameL.setWrapText(true);
        Label brandL = label("by " + m.getBrand(), 11, false, C_MUTED);
        Label priceL = label("₹" + String.format("%.2f", m.getPrice()), 18, true, C_PRIMARY);

        String stockTxt = m.getStock() > 0 ? "✅ In Stock (" + m.getStock() + ")" : "❌ Out of Stock";
        Label stockL = label(stockTxt, 11, false, m.getStock() > 0 ? C_SUCCESS : C_DANGER);

        if (m.isRequiresRx()) {
            Label rx = label("📋 Prescription Required", 11, false, C_ACCENT);
            card.getChildren().add(rx);
        }

        Button addBtn = btn("🛒 Add to Cart", BTN_PRIMARY);
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setDisable(m.getStock() <= 0);
        addBtn.setOnAction(e -> {
            cart.merge(m, 1, Integer::sum);
            alert("✅ " + m.getName() + " added to cart!", Alert.AlertType.INFORMATION);
        });

        card.getChildren().addAll(catBadge, nameL, brandL, priceL, stockL, addBtn);
        return card;
    }

    // ════════════════════════════════════════════════════════
    // CART
    // ════════════════════════════════════════════════════════
    private void showCart() {
        BorderPane root = new BorderPane();
        root.setTop(buildNav());

        VBox body = new VBox(12);
        body.setPadding(new Insets(20));
        body.setStyle("-fx-background-color:" + C_BG + ";");

        Label title = label("🛒 Your Cart", 22, true, C_TEXT);
        body.getChildren().add(title);

        if (cart.isEmpty()) {
            body.getChildren().add(label("Your cart is empty. Browse medicines!", 15, false, C_MUTED));
        } else {
            TableView<CartRow> table = new TableView<>();
            ObservableList<CartRow> rows = FXCollections.observableArrayList();
            cart.forEach((med, qty) -> rows.add(new CartRow(med, qty)));
            table.setItems(rows);

            addCol(table, "Medicine",   "name",     200);
            addCol(table, "Brand",      "brand",    130);
            addCol(table, "Unit Price", "price",    110);
            addCol(table, "Qty",        "qty",       60);
            addCol(table, "Subtotal",   "subtotal", 110);

            TableColumn<CartRow, Void> rmCol = new TableColumn<>("Remove");
            rmCol.setCellFactory(col -> new TableCell<>() {
                final Button b = new Button("✖");
                { b.setStyle(BTN_DANGER); b.setOnAction(e -> { cart.remove(getTableRow().getItem().medicine); showCart(); }); }
                @Override protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : b); }
            });
            rmCol.setPrefWidth(80);
            table.getColumns().add(rmCol);
            VBox.setVgrow(table, Priority.ALWAYS);

            double total = cart.entrySet().stream().mapToDouble(e -> e.getKey().getPrice() * e.getValue()).sum();
            Label totalL  = label("Total: ₹" + String.format("%.2f", total), 18, true, C_PRIMARY);
            TextField adr = field("Delivery Address");
            adr.setText(loggedInCustomer.getAddress());

            ComboBox<String> payBox = new ComboBox<>(FXCollections.observableArrayList("COD", "UPI", "Net Banking", "Card"));
            payBox.setValue("COD"); payBox.setStyle(FIELD_STYLE);

            Button placeBtn = btn("🚀  Place Order", BTN_SUCCESS);
            placeBtn.setOnAction(e -> {
                if (adr.getText().trim().isEmpty()) { alert("Enter delivery address.", Alert.AlertType.WARNING); return; }
                List<Order.OrderItem> items = new ArrayList<>();
                cart.forEach((med, qty) -> items.add(new Order.OrderItem(med.getId(), med.getName(), qty, med.getPrice())));
                try {
                    int id = orderDAO.placeOrder(loggedInCustomer.getId(), total, adr.getText().trim(), payBox.getValue(), items);
                    cart.clear();
                    alert("✅ Order #" + id + " placed! Delivery in 2-4 hours.", Alert.AlertType.INFORMATION);
                    showHome();
                } catch (SQLException ex) { alert("Order failed: " + ex.getMessage(), Alert.AlertType.ERROR); }
            });

            HBox bottom = new HBox(16, totalL, new Region(), payBox, placeBtn);
            HBox.setHgrow(bottom.getChildren().get(1), Priority.ALWAYS);
            bottom.setAlignment(Pos.CENTER_LEFT);

            body.getChildren().addAll(table, adr, bottom);
        }

        root.setCenter(new ScrollPane(body) {{ setFitToWidth(true); }});
        primaryStage.setScene(new Scene(root, 1100, 700));
    }

    // ════════════════════════════════════════════════════════
    // ORDERS
    // ════════════════════════════════════════════════════════
    private void showOrders() {
        BorderPane root = new BorderPane();
        root.setTop(buildNav());
        VBox body = new VBox(12);
        body.setPadding(new Insets(20));
        body.setStyle("-fx-background-color:" + C_BG + ";");
        body.getChildren().add(label("📦 My Orders", 22, true, C_TEXT));
        try {
            List<Order> orders = orderDAO.getOrdersByCustomer(loggedInCustomer.getId());
            if (orders.isEmpty())
                body.getChildren().add(label("No orders yet. Start shopping!", 14, false, C_MUTED));
            else
                for (Order o : orders) body.getChildren().add(buildOrderCard(o));
        } catch (SQLException ex) {
            body.getChildren().add(label("Error: " + ex.getMessage(), 13, false, C_DANGER));
        }
        ScrollPane sp = new ScrollPane(body); sp.setFitToWidth(true);
        root.setCenter(sp);
        primaryStage.setScene(new Scene(root, 1100, 700));
    }

    private HBox buildOrderCard(Order o) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(14)); card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
            "-fx-border-color:#E2E8F0;-fx-border-radius:12;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");

        VBox info = new VBox(4);
        info.getChildren().addAll(
            label("Order #" + o.getId(), 15, true, C_TEXT),
            label("📅 " + (o.getPlacedAt() != null ? o.getPlacedAt().toString().substring(0,16) : ""), 11, false, C_MUTED),
            label("📍 " + o.getDeliveryAddr(), 11, false, C_MUTED)
        );

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        String sc = switch (o.getStatus()) {
            case "DELIVERED" -> C_SUCCESS; case "CANCELLED" -> C_DANGER;
            case "DISPATCHED" -> C_ACCENT; default -> C_PRIMARY;
        };
        VBox right = new VBox(6);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.getChildren().addAll(
            label("₹" + String.format("%.2f", o.getTotalAmount()), 17, true, C_PRIMARY),
            label(o.getStatus(), 12, true, sc)
        );

        card.getChildren().addAll(info, spacer, right);
        return card;
    }

    // ════════════════════════════════════════════════════════
    // PROFILE
    // ════════════════════════════════════════════════════════
    private void showProfile() {
        BorderPane root = new BorderPane();
        root.setTop(buildNav());
        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER); form.setPadding(new Insets(40));
        form.setStyle("-fx-background-color:" + C_BG + ";"); form.setMaxWidth(480);

        TextField nameF  = field("Full Name"); nameF.setText(loggedInCustomer.getFullName()); nameF.setEditable(false);
        TextField emailF = field("Email"); emailF.setText(loggedInCustomer.getEmail()); emailF.setEditable(false);
        TextField phoneF = field("Phone"); phoneF.setText(loggedInCustomer.getPhone());
        TextArea  addrF  = new TextArea(loggedInCustomer.getAddress());
        addrF.setPrefRowCount(3); addrF.setStyle(FIELD_STYLE);

        Label msg = label("", 12, false, C_SUCCESS);
        Button saveBtn = btn("💾  Save Changes", BTN_PRIMARY);
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            try {
                customerDAO.updateProfile(loggedInCustomer.getId(), phoneF.getText().trim(), addrF.getText().trim());
                loggedInCustomer.setPhone(phoneF.getText().trim());
                loggedInCustomer.setAddress(addrF.getText().trim());
                msg.setText("✅ Profile updated successfully!");
            } catch (SQLException ex) { msg.setText("Error: " + ex.getMessage()); }
        });

        form.getChildren().addAll(label("👤 My Profile", 22, true, C_TEXT), nameF, emailF, phoneF, addrF, msg, saveBtn);
        root.setCenter(new StackPane(form));
        primaryStage.setScene(new Scene(root, 1100, 700));
    }

    // ════════════════════════════════════════════════════════
    // ADMIN PANEL
    // ════════════════════════════════════════════════════════
    private void showAdminPanel() {
        Stage s = new Stage();
        s.setTitle("🛠 MediSwift Admin Panel");
        s.setMinWidth(920); s.setMinHeight(600);

        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(buildMedicinesTab(), buildCustomersTab(), buildOrdersTab());
        s.setScene(new Scene(tabs, 960, 640));
        s.show();
    }

    private Tab buildMedicinesTab() {
        Tab tab = new Tab("💊 Medicines"); tab.setClosable(false);
        VBox body = new VBox(12); body.setPadding(new Insets(16));
        body.setStyle("-fx-background-color:" + C_BG + ";");

        TableView<Medicine> table = new TableView<>();
        ObservableList<Medicine> data = FXCollections.observableArrayList();
        Runnable reload = () -> { try { data.setAll(medicineDAO.getAllMedicines()); } catch (SQLException e) { e.printStackTrace(); } };
        reload.run(); table.setItems(data); VBox.setVgrow(table, Priority.ALWAYS);
        addCol(table,"ID","id",50); addCol(table,"Name","name",180); addCol(table,"Brand","brand",120);
        addCol(table,"Category","categoryName",120); addCol(table,"Price","price",90);
        addCol(table,"Stock","stock",70); addCol(table,"Rx","requiresRx",70);

        // Add form
        HBox addRow = new HBox(8);
        TextField nF=field("Name"); nF.setPrefWidth(130);
        TextField bF=field("Brand"); bF.setPrefWidth(100);
        TextField cF=field("CatID"); cF.setPrefWidth(55);
        TextField pF=field("Price"); pF.setPrefWidth(65);
        TextField sF=field("Stock"); sF.setPrefWidth(55);
        CheckBox rxBox=new CheckBox("Rx");
        Button addBtn=btn("➕ Add",BTN_SUCCESS);
        Button delBtn=btn("🗑 Delete",BTN_DANGER);

        addBtn.setOnAction(e -> {
            try {
                medicineDAO.addMedicine(nF.getText(),bF.getText(),Integer.parseInt(cF.getText()),
                    Double.parseDouble(pF.getText()),Integer.parseInt(sF.getText()),"",rxBox.isSelected());
                reload.run(); nF.clear(); bF.clear(); cF.clear(); pF.clear(); sF.clear();
            } catch (Exception ex) { alert("Error: "+ex.getMessage(), Alert.AlertType.ERROR); }
        });
        delBtn.setOnAction(e -> {
            Medicine sel=table.getSelectionModel().getSelectedItem();
            if(sel==null) return;
            try { medicineDAO.deleteMedicine(sel.getId()); reload.run(); }
            catch(SQLException ex){ alert("Cannot delete: "+ex.getMessage(), Alert.AlertType.ERROR); }
        });

        addRow.getChildren().addAll(nF,bF,cF,pF,sF,rxBox,addBtn,delBtn);
        addRow.setAlignment(Pos.CENTER_LEFT);

        body.getChildren().addAll(label("💊 Medicine Management",17,true,C_TEXT), addRow, table);
        tab.setContent(body); return tab;
    }

    private Tab buildCustomersTab() {
        Tab tab = new Tab("👥 Customers"); tab.setClosable(false);
        VBox body = new VBox(12); body.setPadding(new Insets(16));
        body.setStyle("-fx-background-color:"+C_BG+";");
        TableView<Customer> table=new TableView<>();
        ObservableList<Customer> data=FXCollections.observableArrayList();
        try { data.setAll(customerDAO.getAllCustomers()); } catch(SQLException e){ e.printStackTrace(); }
        table.setItems(data); VBox.setVgrow(table,Priority.ALWAYS);
        addCol(table,"ID","id",60); addCol(table,"Name","fullName",160);
        addCol(table,"Email","email",200); addCol(table,"Phone","phone",120);
        body.getChildren().addAll(label("👥 Registered Customers",17,true,C_TEXT), table);
        tab.setContent(body); return tab;
    }

    private Tab buildOrdersTab() {
        Tab tab = new Tab("📋 Orders"); tab.setClosable(false);
        VBox body = new VBox(12); body.setPadding(new Insets(16));
        body.setStyle("-fx-background-color:"+C_BG+";");
        TableView<Order> table=new TableView<>();
        ObservableList<Order> data=FXCollections.observableArrayList();
        try { data.setAll(orderDAO.getAllOrders()); } catch(SQLException e){ e.printStackTrace(); }
        table.setItems(data); VBox.setVgrow(table,Priority.ALWAYS);
        addCol(table,"ID","id",55); addCol(table,"Customer","customerName",140);
        addCol(table,"Total","totalAmount",100); addCol(table,"Status","status",120);
        addCol(table,"Payment","paymentMethod",100); addCol(table,"Date","placedAt",160);

        ComboBox<String> statusBox=new ComboBox<>(FXCollections.observableArrayList(
            "PENDING","CONFIRMED","DISPATCHED","DELIVERED","CANCELLED"));
        statusBox.setStyle(FIELD_STYLE);
        Button updBtn=btn("✏ Update Status",BTN_PRIMARY);
        updBtn.setOnAction(e->{
            Order sel=table.getSelectionModel().getSelectedItem();
            if(sel==null||statusBox.getValue()==null) return;
            try { orderDAO.updateOrderStatus(sel.getId(),statusBox.getValue()); data.setAll(orderDAO.getAllOrders()); }
            catch(SQLException ex){ alert("Error: "+ex.getMessage(),Alert.AlertType.ERROR); }
        });
        HBox actRow=new HBox(10,label("Change status:",13,false,C_MUTED),statusBox,updBtn);
        actRow.setAlignment(Pos.CENTER_LEFT);
        body.getChildren().addAll(label("📋 All Orders",17,true,C_TEXT),actRow,table);
        tab.setContent(body); return tab;
    }

    // ════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════
    private Label label(String txt, int size, boolean bold, String color) {
        Label l = new Label(txt);
        l.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        l.setTextFill(Color.web(color));
        return l;
    }
    private TextField field(String prompt) {
        TextField f = new TextField(); f.setPromptText(prompt); f.setStyle(FIELD_STYLE); return f;
    }
    private PasswordField pass(String prompt) {
        PasswordField f = new PasswordField(); f.setPromptText(prompt); f.setStyle(FIELD_STYLE); return f;
    }
    private Button btn(String txt, String style) {
        Button b = new Button(txt); b.setStyle(style); return b;
    }
    private Button navBtn(String txt) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:transparent;-fx-text-fill:white;" +
                   "-fx-font-size:13;-fx-cursor:hand;-fx-font-weight:bold;");
        return b;
    }
    private Button linkBtn(String txt) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:transparent;-fx-text-fill:#0EA5E9;" +
                   "-fx-font-size:13;-fx-cursor:hand;-fx-underline:true;-fx-border-color:transparent;");
        return b;
    }
    private <T> void addCol(TableView<T> t, String h, String prop, double w) {
        TableColumn<T,?> c = new TableColumn<>(h);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w); t.getColumns().add(c);
    }
    private void alert(String msg, Alert.AlertType type) {
        new Alert(type, msg) {{ setHeaderText(null); showAndWait(); }};
    }

    // CartRow for TableView
    public static class CartRow {
        final Medicine medicine;
        private final StringProperty  name, brand;
        private final DoubleProperty  price, subtotal;
        private final IntegerProperty qty;
        CartRow(Medicine m, int quantity) {
            medicine = m;
            name     = new SimpleStringProperty(m.getName());
            brand    = new SimpleStringProperty(m.getBrand());
            price    = new SimpleDoubleProperty(m.getPrice());
            qty      = new SimpleIntegerProperty(quantity);
            subtotal = new SimpleDoubleProperty(m.getPrice() * quantity);
        }
        public StringProperty  nameProperty()     { return name; }
        public StringProperty  brandProperty()    { return brand; }
        public DoubleProperty  priceProperty()    { return price; }
        public IntegerProperty qtyProperty()      { return qty; }
        public DoubleProperty  subtotalProperty() { return subtotal; }
    }

    @Override public void stop() { DBConnection.closeConnection(); }

    public static void main(String[] args) { launch(args); }
}
