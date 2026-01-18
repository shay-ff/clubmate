package clubmate.backend.auth.repository;

import clubmate.backend.auth.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID>, UserDetailsService {

    /**
     * Find an admin by username.
     */
    Optional<Admin> findByUsername(String username);

    /**
     * Check if an admin with the given username exists.
     */
    boolean existsByUsername(String username);

    /**
     * Find active admins by username.
     */
    Optional<Admin> findByUsernameAndActiveTrue(String username);

    /**
     * Load user details for Spring Security.
     */
    @Override
    default UserDetails loadUserByUsername(String username) {
        return findByUsernameAndActiveTrue(username)
                .map(admin -> User.builder()
                        .username(admin.getUsername())
                        .password(admin.getPasswordHash())
                        .roles("ADMIN")
                        .accountLocked(!admin.getActive())
                        .build())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                    "Admin not found: " + username
                ));
    }
}
