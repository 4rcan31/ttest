CREATE TABLE cart_items (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    product_id BIGINT      NOT NULL,
    quantity   INT         NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_cart_items PRIMARY KEY (id),
    CONSTRAINT uk_cart_items_user_product UNIQUE (user_id, product_id),
    CONSTRAINT ck_cart_items_quantity CHECK (quantity > 0)
);

CREATE TABLE orders (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    order_number     VARCHAR(20)    NOT NULL,
    user_id          BIGINT         NOT NULL,
    customer_name    VARCHAR(170)   NOT NULL,
    customer_email   VARCHAR(120)   NOT NULL,
    shipping_address VARCHAR(255)   NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    item_count       INT            NOT NULL,
    subtotal         DECIMAL(12, 2) NOT NULL,
    shipping_cost    DECIMAL(12, 2) NOT NULL,
    total            DECIMAL(12, 2) NOT NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    created_at       DATETIME(6)    NOT NULL,
    updated_at       DATETIME(6)    NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT uk_orders_order_number UNIQUE (order_number)
);

CREATE INDEX idx_orders_user_created ON orders (user_id, created_at);
CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE order_items (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    sku          VARCHAR(20)    NOT NULL,
    product_name VARCHAR(120)   NOT NULL,
    image_url    VARCHAR(255)   NOT NULL,
    unit_price   DECIMAL(10, 2) NOT NULL,
    quantity     INT            NOT NULL,
    line_total   DECIMAL(12, 2) NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order ON order_items (order_id);

CREATE TABLE order_status_history (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    order_id   BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL,
    changed_by VARCHAR(20) NOT NULL,
    changed_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_order_status_history PRIMARY KEY (id),
    CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_status_history_order ON order_status_history (order_id);
