CREATE TABLE categories (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    slug VARCHAR(40) NOT NULL,
    name VARCHAR(60) NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uk_categories_slug UNIQUE (slug)
);

CREATE TABLE products (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    sku         VARCHAR(20)    NOT NULL,
    name        VARCHAR(120)   NOT NULL,
    description VARCHAR(500)   NOT NULL,
    brand       VARCHAR(60)    NOT NULL,
    category_id BIGINT         NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    stock       INT            NOT NULL,
    image_url   VARCHAR(255)   NOT NULL,
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT ck_products_stock CHECK (stock >= 0),
    CONSTRAINT ck_products_price CHECK (price > 0)
);

CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_price ON products (price);
