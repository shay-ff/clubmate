package model;

import java.time.Instant;
import java.util.UUID;

public class AuditLog {
    UUID id;
    UUID adminId;
    String action;
    Instant createdAt;
}
