CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CART' CHECK (status IN ('CART', 'PENDING', 'CONFIRMED', 'PAID', 'PREPARED', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED')),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (subtotal >= 0),
    shipping_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (shipping_cost >= 0),
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (tax_amount >= 0),
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (discount_amount >= 0),
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (total_amount >= 0),
    
    client_email VARCHAR(255) NOT NULL,
    client_first_name VARCHAR(100) NOT NULL,
    client_last_name VARCHAR(100) NOT NULL,
    client_phone VARCHAR(20),
    
    shipping_address JSONB NOT NULL,
    billing_address JSONB NOT NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    validated_at TIMESTAMP,
    paid_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    
    carrier VARCHAR(100),
    tracking_number VARCHAR(100),
    client_comment TEXT,
    internal_comment TEXT
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_description TEXT,
    product_sku VARCHAR(100) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    
    image_url VARCHAR(500),
    unit_weight DECIMAL(8,2),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    comment TEXT,
    user_id BIGINT, -- ID of user who changed status
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_client_id ON orders(client_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_email ON orders(client_email);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_sku ON order_items(product_sku);

CREATE INDEX idx_status_history_order_id ON status_history(order_id);
CREATE INDEX idx_status_history_created_at ON status_history(created_at);

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER orders_update_trigger
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER order_items_update_trigger
    BEFORE UPDATE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.order_number IS NULL OR NEW.order_number = '' THEN
        NEW.order_number = 'ORD-' || TO_CHAR(NEW.created_at, 'YYYYMMDD') || '-' || LPAD(NEW.id::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generate_order_number_trigger
    BEFORE INSERT ON orders
    FOR EACH ROW
    EXECUTE FUNCTION generate_order_number();

CREATE OR REPLACE FUNCTION log_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO status_history (order_id, previous_status, new_status, comment)
        VALUES (NEW.id, OLD.status, NEW.status, 'Automatic status change');
        
        CASE NEW.status
            WHEN 'CONFIRMED' THEN NEW.validated_at = CURRENT_TIMESTAMP;
            WHEN 'PAID' THEN NEW.paid_at = CURRENT_TIMESTAMP;
            WHEN 'SHIPPED' THEN NEW.shipped_at = CURRENT_TIMESTAMP;
            WHEN 'DELIVERED' THEN NEW.delivered_at = CURRENT_TIMESTAMP;
            ELSE NULL;
        END CASE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER status_change_trigger
    AFTER UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION log_status_change();

CREATE OR REPLACE FUNCTION recalculate_totals()
RETURNS TRIGGER AS $$
DECLARE
    order_record RECORD;
    new_subtotal DECIMAL(10,2);
BEGIN
    SELECT * INTO order_record FROM orders WHERE id = COALESCE(NEW.order_id, OLD.order_id);
    
    SELECT COALESCE(SUM(subtotal), 0.00) INTO new_subtotal
    FROM order_items 
    WHERE order_id = order_record.id;
    
    UPDATE orders 
    SET 
        subtotal = new_subtotal,
        total_amount = new_subtotal + shipping_cost + tax_amount - discount_amount
    WHERE id = order_record.id;
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER recalculate_totals_trigger
    AFTER INSERT OR UPDATE OR DELETE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION recalculate_totals();

INSERT INTO orders (
    client_id, order_number, status, client_email, client_first_name, client_last_name, client_phone,
    shipping_address, billing_address, shipping_cost, tax_amount
) VALUES 
(
    2, 'ORD-20241201-000001', 'CONFIRMED', 'client@test.com', 'Jean', 'Dupont', '0123456789',
    '{"street": "123 Rue de la Paix", "city": "Paris", "postal_code": "75001", "country": "France"}',
    '{"street": "123 Rue de la Paix", "city": "Paris", "postal_code": "75001", "country": "France"}',
    9.99, 0.00
),
(
    3, 'ORD-20241201-000002', 'CART', 'marie@test.com', 'Marie', 'Martin', '0123456789',
    '{"street": "456 Avenue des Champs", "city": "Lyon", "postal_code": "69000", "country": "France"}',
    '{"street": "456 Avenue des Champs", "city": "Lyon", "postal_code": "69000", "country": "France"}',
    0.00, 0.00
);

INSERT INTO order_items (
    order_id, product_id, product_name, product_sku, unit_price, quantity, subtotal, image_url
) VALUES 
(1, 1, 'iPhone 15 Pro', 'APPLE-IP15P-128', 1199.99, 1, 1199.99, '/images/iphone15pro.jpg'),
(1, 5, 'AirPods Pro', 'APPLE-APP-GEN2', 279.99, 1, 279.99, '/images/airpodspro.jpg'),
(2, 2, 'Samsung Galaxy S24', 'SAMSUNG-GS24-256', 899.99, 1, 899.99, '/images/galaxys24.jpg');

COMMIT;
