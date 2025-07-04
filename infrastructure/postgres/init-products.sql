CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock_available INTEGER NOT NULL DEFAULT 0 CHECK (stock_available >= 0),
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    image_url VARCHAR(500),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_categories_active ON categories(active);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_stock ON products(stock_available);
CREATE INDEX idx_products_price ON products(price);

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER categories_update_trigger
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER products_update_trigger
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

INSERT INTO categories (name, description) VALUES
('Electronics', 'Electronic devices and accessories'),
('Clothing', 'Clothing for men, women and children'),
('Books', 'Books, magazines and publications'),
('Home & Garden', 'Home and garden items'),
('Sports & Recreation', 'Sports and recreational equipment');

INSERT INTO categories (name, description, parent_id) VALUES
('Smartphones', 'Mobile phones and smartphones', 1),
('Computers', 'Laptops and desktop computers', 1),
('Accessories', 'Electronic accessories', 1),
('Men', 'Men clothing', 2),
('Women', 'Women clothing', 2),
('Children', 'Children clothing', 2);

-- Table pour l'historique des mouvements de stock
CREATE TABLE stock_history (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    movement_type VARCHAR(20) NOT NULL CHECK (movement_type IN ('ORDER_REDUCTION', 'ORDER_CANCELLATION', 'MANUAL_ADJUSTMENT', 'INBOUND', 'OUTBOUND', 'ADJUSTMENT')),
    quantity INTEGER NOT NULL,
    previous_stock INTEGER NOT NULL,
    new_stock INTEGER NOT NULL,
    order_id BIGINT, -- Référence à la commande (nullable pour ajustements manuels)
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour optimiser les requêtes sur l'historique
CREATE INDEX idx_stock_history_product ON stock_history(product_id);
CREATE INDEX idx_stock_history_order ON stock_history(order_id);
CREATE INDEX idx_stock_history_type ON stock_history(movement_type);
CREATE INDEX idx_stock_history_created ON stock_history(created_at);

INSERT INTO products (name, description, price, stock_available, category_id, sku, image_url) VALUES
('iPhone 15 Pro', 'Apple iPhone 15 Pro 128GB Smartphone', 1199.99, 50, 6, 'APPLE-IP15P-128', '/images/iphone15pro.jpg'),
('Samsung Galaxy S24', 'Samsung Galaxy S24 256GB Smartphone', 899.99, 30, 6, 'SAMSUNG-GS24-256', '/images/galaxys24.jpg'),
('MacBook Air M2', 'Apple MacBook Air M2 13" Laptop', 1299.99, 25, 7, 'APPLE-MBA-M2-13', '/images/macbookair.jpg'),
('Dell XPS 13', 'Dell XPS 13 Plus Laptop', 1099.99, 20, 7, 'DELL-XPS13-PLUS', '/images/dellxps13.jpg'),
('AirPods Pro', 'Apple AirPods Pro 2nd Generation Wireless Earbuds', 279.99, 100, 8, 'APPLE-APP-GEN2', '/images/airpodspro.jpg');

COMMIT;
