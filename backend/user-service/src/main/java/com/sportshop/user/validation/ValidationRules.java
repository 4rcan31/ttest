package com.sportshop.user.validation;

/** Reglas de validación compartidas por los DTOs (el frontend replica las mismas reglas). */
public final class ValidationRules {

    /** Letras (incluye tildes y ñ), espacios, apóstrofes, puntos y guiones. */
    public static final String NAME_PATTERN = "^[\\p{L}][\\p{L} '.-]*$";

    /** Formato usuario@dominio.tld; más estricto que {@code @Email}, que acepta "a@b". */
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /** Mínimo 8 caracteres con al menos una mayúscula, una minúscula y un número (máx. 72 por BCrypt). */
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$";

    public static final String PASSWORD_MESSAGE =
            "La contraseña debe tener entre 8 y 72 caracteres e incluir mayúscula, minúscula y número";

    public static final int MIN_AGE = 18;

    private ValidationRules() {
    }
}
