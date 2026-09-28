package com.sportshop.user.validation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    private static final int MAX_AGE = 120;

    private final Clock clock;
    private int minAge = ValidationRules.MIN_AGE;

    public AdultValidator() {
        this(Clock.systemDefaultZone());
    }

    AdultValidator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void initialize(Adult annotation) {
        this.minAge = annotation.minAge();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return true; // @NotNull se encarga del campo obligatorio
        }
        LocalDate today = LocalDate.now(clock);
        if (birthDate.isAfter(today)) {
            return false;
        }
        int age = Period.between(birthDate, today).getYears();
        return age >= minAge && age <= MAX_AGE;
    }
}
