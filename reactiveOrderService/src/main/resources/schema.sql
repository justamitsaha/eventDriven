use `amit`;
CREATE TABLE orders (
    order_id CHAR(36) NOT NULL PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at BIGINT NOT NULL
);
