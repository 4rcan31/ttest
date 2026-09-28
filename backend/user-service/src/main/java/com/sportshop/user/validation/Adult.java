package com.sportshop.user.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/** Valida que la fecha de nacimiento corresponda a una persona con al menos {@link #minAge()} años. */
@Documented
@Constraint(validatedBy = AdultValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Adult {

    String message() default "Debe ser mayor de 18 años";

    int minAge() default ValidationRules.MIN_AGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
