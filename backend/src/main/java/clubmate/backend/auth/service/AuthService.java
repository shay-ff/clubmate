package clubmate.backend.auth.service;

import clubmate.backend.auth.dto.LoginRequestDTO;
import clubmate.backend.auth.dto.LoginResponseDTO;
import clubmate.backend.auth.model.Admin;
import clubmate.backend.auth.repository.AdminRepository;
import clubmate.backend.auth.security.JwtTokenProvider;
import clubmate.backend.common.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for admin authentication and login.
 */
@Slf4j
@Service
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @Autowired
    public AuthService(AdminRepository adminRepository,
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider jwtTokenProvider) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Authenticate admin and generate JWT tokens.
     *
     * @param loginRequest Admin credentials
     * @return LoginResponseDTO with access and refresh tokens
     * @throws ValidationException if credentials are invalid
     */
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Login attempt for admin: {}", loginRequest.getUsername());

        // Find admin by username
        Admin admin = adminRepository.findByUsernameAndActiveTrue(loginRequest.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed: Admin not found - {}", loginRequest.getUsername());
                    return new ValidationException("Invalid username or password");
                });

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), admin.getPasswordHash())) {
            log.warn("Login failed: Invalid password for admin - {}", loginRequest.getUsername());
            throw new ValidationException("Invalid username or password");
        }

        // Update last login
        admin.updateLastLogin();
        adminRepository.save(admin);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateToken(admin.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(admin.getUsername());

        log.info("Admin logged in successfully: {}", admin.getUsername());

        return new LoginResponseDTO(accessToken, refreshToken, jwtExpirationMs / 1000);
    }

    /**
     * Register a new admin (for initial setup).
     *
     * @param username Admin username
     * @param password Admin password
     * @param email    Admin email
     * @return Created Admin
     * @throws ValidationException if username already exists
     */
    public Admin registerAdmin(String username, String password, String email) {
        log.info("Registering new admin: {}", username);

        if (adminRepository.existsByUsername(username)) {
            throw new ValidationException("Username already exists: " + username);
        }

        String hashedPassword = passwordEncoder.encode(password);
        Admin admin = new Admin(username, hashedPassword, email);
        Admin savedAdmin = adminRepository.save(admin);

        log.info("Admin registered successfully: {}", username);
        return savedAdmin;
    }
}
