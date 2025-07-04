

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(50) DEFAULT 'CLIENT' CHECK (role IN ('CLIENT', 'ADMIN')),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('SHIPPING', 'BILLING')),
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'France',
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clients_email ON clients(email);
CREATE INDEX idx_clients_active ON clients(active);
CREATE INDEX idx_addresses_client_id ON addresses(client_id);
CREATE INDEX idx_addresses_type ON addresses(type);

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER clients_update_trigger
    BEFORE UPDATE ON clients
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER addresses_update_trigger
    BEFORE UPDATE ON addresses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

CREATE UNIQUE INDEX idx_addresses_primary 
ON addresses (client_id, type) 
WHERE is_primary = true;

INSERT INTO clients (email, password, first_name, last_name, phone, role) VALUES
('admin@microcommerce.com', '$2a$12$iLpx9KkSoRmesQMpDmD3Iug60gAv3GmCJjM.dZQsl2HmkVc56rwty', 'Admin', 'System', '0123456789', 'ADMIN'),
('client@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jean', 'Dupont', '0123456789', 'CLIENT'),
('marie@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Marie', 'Martin', '0123456789', 'CLIENT');

INSERT INTO addresses (client_id, type, street, city, postal_code, country, is_primary) VALUES
(2, 'SHIPPING', '123 Rue de la Paix', 'Paris', '75001', 'France', true),
(2, 'BILLING', '123 Rue de la Paix', 'Paris', '75001', 'France', true),
(3, 'SHIPPING', '456 Avenue des Champs', 'Lyon', '69000', 'France', true);

COMMIT;
