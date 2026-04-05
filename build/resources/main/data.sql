-- Seed data for the products table.
-- TRUNCATE clears all rows. The explicit RESTART resets H2's AUTO_INCREMENT
-- sequence to 1, ensuring products receive predictable IDs (1, 2, 3) on
-- every application start regardless of H2 version behaviour.
TRUNCATE TABLE products;
ALTER TABLE products ALTER COLUMN id RESTART WITH 1;

INSERT INTO products (name, price) VALUES
    ('Wireless Headphones', 79.99),
    ('Mechanical Keyboard', 129.99),
    ('USB-C Hub', 49.99);
