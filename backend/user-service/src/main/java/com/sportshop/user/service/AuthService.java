package com.sportshop.user.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportshop.common.web.ApiException;
import com.sportshop.user.config.UserServiceProperties;
import com.sportshop.user.domain.Role;
import com.sportshop.user.domain.User;
import com.sportshop.user.dto.AuthResponse;
import com.sportshop.user.dto.LoginRequest;
import com.sportshop.user.dto.RegisterRequest;
import com.sportshop.user.dto.UserResponse;
import com.sportshop.user.repository.UserRepository;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String INVALID_CREDENTIALS = "Correo electrónico o contraseña incorrectos";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserServiceProperties properties;
    private final Clock clock;
    /** Hash ficticio para igualar el tiempo de respuesta cuando el correo no existe (evita enumeración). */
    private final String dummyHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService,
                       UserServiceProperties properties, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.properties = properties;
        this.clock = clock;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("EMAIL_ALREADY_REGISTERED", "Ya existe una cuenta registrada con ese correo");
        }
        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setShippingAddress(request.shippingAddress().trim());
        user.setBirthDate(request.birthDate());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException concurrentRegistration) {
            // Dos registros simultáneos con el mismo correo: la restricción única decide.
            throw ApiException.conflict("EMAIL_ALREADY_REGISTERED", "Ya existe una cuenta registrada con ese correo");
        }
        log.info("Usuario registrado id={}", user.getId());
        return buildAuthResponse(user);
    }

    /**
     * Autentica al usuario. Los intentos fallidos se guardan aunque se lance la excepción
     * ({@code noRollbackFor}) para poder bloquear la cuenta temporalmente.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse login(LoginRequest request) {
        Instant now = clock.instant();
        User user = userRepository.findByEmail(normalizeEmail(request.email())).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyHash);
            throw ApiException.unauthorized("INVALID_CREDENTIALS", INVALID_CREDENTIALS);
        }
        if (user.isLocked(now)) {
            throw new ApiException(HttpStatus.LOCKED, "ACCOUNT_LOCKED",
                    "La cuenta está bloqueada temporalmente por varios intentos fallidos. Intente más tarde "
                            + "o restablezca su contraseña.");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            UserServiceProperties.Login policy = properties.login();
            user.registerFailedLogin(policy.maxFailedAttempts(), now.plus(policy.lockDuration()));
            throw ApiException.unauthorized("INVALID_CREDENTIALS", INVALID_CREDENTIALS);
        }
        user.resetLoginAttempts();
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.bearer(tokenService.issueToken(user), tokenService.expiresInSeconds(),
                UserResponse.from(user));
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
