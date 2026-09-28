package com.sportshop.user.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.sportshop.user.domain.User;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String shippingAddress,
        LocalDate birthDate,
        String role,
        Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getShippingAddress(), user.getBirthDate(), user.getRole().name(), user.getCreatedAt());
    }
}
