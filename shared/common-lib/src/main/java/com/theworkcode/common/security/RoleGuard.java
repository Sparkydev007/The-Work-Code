package com.theworkcode.common.security;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Server-side authorization helper. The gateway forwards the authenticated
 * role via {@code X-Forwarded-User-Role}; every service MUST re-check
 * capabilities here rather than trusting the frontend.
 */
public final class RoleGuard {

    public static final String HEADER_ROLE = "X-Forwarded-User-Role";
    public static final String HEADER_USER = "X-Forwarded-User";

    private RoleGuard() {
    }

    /** Current role or null when unauthenticated (should not happen behind the gateway). */
    public static Role currentRole(HttpServletRequest request) {
        String role = request.getHeader(HEADER_ROLE);
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String currentUser(HttpServletRequest request) {
        return request.getHeader(HEADER_USER);
    }

    /** Throws 403 when the caller lacks the capability. ADMIN passes everything. */
    public static void require(HttpServletRequest request, String capability) {
        Role role = currentRole(request);
        if (role == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!role.has(capability)) {
            throw new ApiException(ErrorCode.FORBIDDEN,
                    "Role " + role + " does not allow '" + capability + "'.");
        }
    }
}
