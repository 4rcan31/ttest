package com.sportshop.user.notification;

import java.time.Duration;

public record PasswordResetRequestedEvent(Long userId, String email, String firstName, String resetLink,
                                          Duration validity) {
}
