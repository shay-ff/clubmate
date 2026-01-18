package clubmate.backend.auth.controller;

import clubmate.backend.auth.dto.LoginRequestDTO;
import clubmate.backend.auth.dto.LoginResponseDTO;
import clubmate.backend.auth.service.AuthService;
import clubmate.backend.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for admin authentication.
 * Login endpoint is public; other operations are admin-only.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Admin login endpoint.
     * PUBLIC endpoint - no authentication required.
     *
     * @param loginRequest Admin credentials (username and password)
     * @return LoginResponseDTO with access and refresh tokens
     *
     * Example request:
     * POST /api/auth/login
     * {
     *   "username": "admin",
     *   "password": "password123"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @RequestBody LoginRequestDTO loginRequest) {
        
        log.info("Login request for admin: {}", loginRequest.getUsername());

        try {
            LoginResponseDTO response = authService.login(loginRequest);
            log.info("Admin login successful: {}", loginRequest.getUsername());
            
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));

        } catch (Exception e) {
            log.warn("Login failed for admin {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication Failed", e.getMessage()));
        }
    }

    /**
     * Health check endpoint.
     * PUBLIC endpoint.
     *
     * @return OK status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Controller is healthy");
    }
}
