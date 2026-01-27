--liquibase formatted sql

--changeset admin:1
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    sale BIGINT DEFAULT 0,
    total_price DOUBLE PRECISION NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--changeset admin:2
CREATE INDEX idx_orders_order_id ON orders(order_id);

--changeset admin:3
CREATE INDEX idx_orders_user_id ON orders(user_id);

--changeset admin:4
CREATE INDEX idx_orders_product_id ON orders(product_id);

--changeset admin:5
COMMENT ON TABLE orders IS 'Table for storing read-only order data from Kafka. Used for analytics and reporting.';
COMMENT ON COLUMN orders.id IS 'Unique record identifier (auto-increment)';
COMMENT ON COLUMN orders.order_id IS 'Order identifier from Order Service (can repeat for different products in the same order)';
COMMENT ON COLUMN orders.product_id IS 'Product identifier from Inventory Service';
COMMENT ON COLUMN orders.user_id IS 'User identifier who placed the order';
COMMENT ON COLUMN orders.quantity IS 'Number of product units in the order';
COMMENT ON COLUMN orders.price IS 'Base product price without discount';
COMMENT ON COLUMN orders.sale IS 'Product discount in percentage (0-100)';
COMMENT ON COLUMN orders.total_price IS 'Final price including discount and quantity';
COMMENT ON COLUMN orders.created_at IS 'Timestamp of record creation in DB (auto-populated)';