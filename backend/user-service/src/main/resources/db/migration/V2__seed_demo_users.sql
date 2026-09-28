-- Usuarios de demostración. Contraseña de ambos: Demo1234 (hash BCrypt, costo 12).
INSERT INTO users (first_name, last_name, email, shipping_address, birth_date, password_hash, role,
                   failed_login_attempts, created_at, updated_at)
VALUES ('Administrador', 'SportShop', 'admin@sportshop.com',
        'Oficinas centrales, Boulevard Los Próceres, Torre 1, Nivel 5', '1990-01-15',
        '$2a$12$q0siMxuSdU/vr56xRJ/AieAM68EeUNZF6LdKR.IgxHzjCozNYWBiy', 'ADMIN', 0,
        CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
       ('María José', 'Pérez López', 'cliente@sportshop.com',
        'Colonia Escalón, Calle La Mascota #123, San Salvador', '1995-06-20',
        '$2a$12$q0siMxuSdU/vr56xRJ/AieAM68EeUNZF6LdKR.IgxHzjCozNYWBiy', 'CUSTOMER', 0,
        CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
