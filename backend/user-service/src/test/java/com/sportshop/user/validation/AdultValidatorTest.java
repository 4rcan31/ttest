package com.sportshop.user.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdultValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 28);

    private AdultValidator validator;

    @BeforeEach
    void setUp() {
        Clock fixed = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        validator = new AdultValidator(fixed);
    }

    @Test
    void acceptsPersonTurning18Today() {
        assertThat(validator.isValid(TODAY.minusYears(18), null)).isTrue();
    }

    @Test
    void rejectsPersonTurning18Tomorrow() {
        assertThat(validator.isValid(TODAY.minusYears(18).plusDays(1), null)).isFalse();
    }

    @Test
    void rejectsFutureDates() {
        assertThat(validator.isValid(TODAY.plusDays(1), null)).isFalse();
    }

    @Test
    void rejectsUnrealisticAges() {
        assertThat(validator.isValid(TODAY.minusYears(130), null)).isFalse();
    }

    @Test
    void nullIsDelegatedToNotNull() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
