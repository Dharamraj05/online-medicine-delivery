-- ============================================================
-- MediSwift - Online Medicine Delivery App
-- MySQL Schema + Seed Data
-- ============================================================

CREATE DATABASE IF NOT EXISTS mediswift;
USE mediswift;

-- ─── CUSTOMERS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS customers (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    phone        VARCHAR(15)  NOT NULL,
    address      TEXT         NOT NULL,
    password     VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─── CATEGORIES ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS categories (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(80) NOT NULL UNIQUE
);

-- ─── MEDICINES ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS medicines (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    brand          VARCHAR(100),
    category_id    INT NOT NULL,
    price          DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    description    TEXT,
    requires_rx    BOOLEAN DEFAULT FALSE,
    image_url      VARCHAR(255),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- ─── ORDERS ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    customer_id     INT NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    delivery_addr   TEXT NOT NULL,
    status          ENUM('PENDING','CONFIRMED','DISPATCHED','DELIVERED','CANCELLED')
                    NOT NULL DEFAULT 'PENDING',
    payment_method  VARCHAR(30) DEFAULT 'COD',
    placed_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- ─── ORDER ITEMS ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_items (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    order_id    INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity    INT NOT NULL,
    unit_price  DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id)    REFERENCES orders(id),
    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

-- ─── CART ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS cart (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity    INT NOT NULL DEFAULT 1,
    added_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

-- ─── SEED DATA ────────────────────────────────────────────────
INSERT IGNORE INTO categories (name) VALUES
    ('Antibiotics'), ('Pain Relief'), ('Vitamins & Supplements'),
    ('Diabetes Care'), ('Heart & Blood Pressure'), ('Cold & Flu'),
    ('Skin Care'), ('Digestive Health');

INSERT IGNORE INTO customers (full_name, email, phone, address, password) VALUES
    ('Rahul Sharma',  'rahul@example.com',  '9876543210', '12 MG Road, Delhi',        'pass123'),
    ('Priya Singh',   'priya@example.com',  '9123456789', '45 Park Street, Mumbai',   'pass123'),
    ('Amit Kumar',    'amit@example.com',   '9988776655', '78 Civil Lines, Lucknow',   'pass123');

INSERT IGNORE INTO medicines (name, brand, category_id, price, stock_quantity, description, requires_rx) VALUES
    ('Amoxicillin 500mg',   'Cipla',      1, 120.00, 200, 'Broad-spectrum antibiotic for bacterial infections.',     TRUE),
    ('Azithromycin 250mg',  'Sun Pharma', 1,  95.00, 150, 'Antibiotic for respiratory tract infections.',            TRUE),
    ('Paracetamol 650mg',   'Cipla',      2,  25.00, 500, 'Fever and mild-to-moderate pain relief.',                 FALSE),
    ('Ibuprofen 400mg',     'Abbott',     2,  40.00, 400, 'Anti-inflammatory painkiller.',                           FALSE),
    ('Vitamin C 1000mg',    'HealthViva', 3,  199.00, 300, 'Immunity booster, antioxidant supplement.',              FALSE),
    ('Vitamin D3 60K IU',   'TrueBasics', 3,  250.00, 180, 'Bone health and immunity support.',                      FALSE),
    ('Metformin 500mg',     'Cipla',      4,  55.00, 250, 'First-line medication for type 2 diabetes.',              TRUE),
    ('Glimepiride 1mg',     'Sanofi',     4,  70.00, 200, 'Controls blood sugar levels in diabetes.',                TRUE),
    ('Amlodipine 5mg',      'Pfizer',     5,  60.00, 220, 'Calcium channel blocker for hypertension.',               TRUE),
    ('Atorvastatin 10mg',   'Sun Pharma', 5,  85.00, 180, 'Lowers cholesterol and triglycerides.',                   TRUE),
    ('Cetirizine 10mg',     'Cipla',      6,  30.00, 400, 'Antihistamine for cold and allergy relief.',              FALSE),
    ('Cough Syrup 100ml',   'Benadryl',   6,  95.00, 300, 'Relieves cough and throat irritation.',                   FALSE),
    ('Clotrimazole Cream',  'GSK',        7, 110.00, 150, 'Antifungal cream for skin infections.',                   FALSE),
    ('Pantoprazole 40mg',   'Sun Pharma', 8,  65.00, 350, 'Proton pump inhibitor for acidity and GERD.',             TRUE),
    ('ORS Powder',          'Electral',   8,  45.00, 500, 'Oral rehydration salts for diarrhoea.',                   FALSE);
