package com.manish.kitchenpulse.identity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final String INVALID = "Invalid email or password";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Compared against when the email is unknown, so timing does not reveal which emails exist. */
    private final String dummyHash;

    AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    public LoginResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim()).orElse(null);

        String hashToCheck = (user != null) ? user.getPasswordHash() : dummyHash;
        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToCheck);

        // One generic message for unknown email, wrong password and deactivated account.
        if (user == null || !passwordMatches || !user.isActive()) {
            throw new BadCredentialsException(INVALID);
        }

        return new LoginResponse(
                jwtService.generateToken(user.getId()),
                "Bearer",
                jwtService.expiresInSeconds(),
                UserResponse.from(user));
    }
}
