package model;

import java.time.Instant;
import java.util.UUID;

public class RefreshToken {
    private UUID id;
    private UUID adminId;
    private String token;
    Instant expiresAt;
    boolean revoked;


}
