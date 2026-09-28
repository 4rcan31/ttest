package com.sportshop.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportshop.common.web.ApiException;
import com.sportshop.user.domain.User;
import com.sportshop.user.dto.ChangePasswordRequest;
import com.sportshop.user.dto.UpdateAddressRequest;
import com.sportshop.user.dto.UpdateProfileRequest;
import com.sportshop.user.dto.UserResponse;
import com.sportshop.user.repository.UserRepository;

/** Consulta y mantenimiento del perfil del cliente autenticado. */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return UserResponse.from(findUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        String email = AuthService.normalizeEmail(request.email());
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw ApiException.conflict("EMAIL_ALREADY_REGISTERED", "Ya existe una cuenta registrada con ese correo");
        }
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setShippingAddress(request.shippingAddress().trim());
        user.setBirthDate(request.birthDate());
        try {
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException concurrentUpdate) {
            throw ApiException.conflict("EMAIL_ALREADY_REGISTERED", "Ya existe una cuenta registrada con ese correo");
        }
    }

    @Transactional
    public UserResponse updateShippingAddress(Long userId, UpdateAddressRequest request) {
        User user = findUser(userId);
        user.setShippingAddress(request.shippingAddress().trim());
        return UserResponse.from(userRepository.saveAndFlush(user));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("INVALID_CURRENT_PASSWORD", "La contraseña actual no es correcta");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("SAME_PASSWORD", "La nueva contraseña debe ser distinta a la actual");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void deleteAccount(Long userId) {
        userRepository.delete(findUser(userId));
        log.info("Cuenta eliminada id={}", userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "El usuario no existe"));
    }
}
