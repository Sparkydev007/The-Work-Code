package com.theworkcode.common.security;

import java.util.Set;

/**
 * Roles for RBAC. Authorization is enforced server-side only.
 */
public enum Role {
    ADMIN(Set.of("*")),
    ANALYST(Set.of("verification", "employees", "employers", "income", "identity", "risk", "reports", "batch", "disputes", "audit:read", "analytics")),
    DEVELOPER(Set.of("api-keys", "webhooks", "api-logs", "docs", "audit:read", "analytics")),
    VIEWER(Set.of("verification:read", "employees:read", "employers:read", "income:read", "reports:read", "analytics"));

    private final Set<String> capabilities;

    Role(Set<String> capabilities) {
        this.capabilities = capabilities;
    }

    public Set<String> capabilities() {
        return capabilities;
    }

    public boolean has(String capability) {
        return capabilities.contains("*") || capabilities.contains(capability);
    }
}
