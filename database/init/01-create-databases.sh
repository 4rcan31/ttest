#!/bin/bash
# Se ejecuta una sola vez al inicializar el volumen de MariaDB.
# Patrón "database per service": cada microservicio tiene su propio esquema y un usuario con
# privilegios solo sobre ese esquema (principio de mínimo privilegio). Las tablas las crea Flyway.
set -euo pipefail

mariadb --protocol=socket -uroot -p"${MARIADB_ROOT_PASSWORD}" <<-EOSQL
    CREATE DATABASE IF NOT EXISTS sportshop_users   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE DATABASE IF NOT EXISTS sportshop_catalog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE DATABASE IF NOT EXISTS sportshop_orders  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    CREATE USER IF NOT EXISTS 'users_svc'@'%'   IDENTIFIED BY '${USERS_DB_PASSWORD}';
    CREATE USER IF NOT EXISTS 'catalog_svc'@'%' IDENTIFIED BY '${CATALOG_DB_PASSWORD}';
    CREATE USER IF NOT EXISTS 'orders_svc'@'%'  IDENTIFIED BY '${ORDERS_DB_PASSWORD}';

    GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON sportshop_users.*   TO 'users_svc'@'%';
    GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON sportshop_catalog.* TO 'catalog_svc'@'%';
    GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON sportshop_orders.*  TO 'orders_svc'@'%';
    FLUSH PRIVILEGES;
EOSQL
