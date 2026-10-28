package com.vanish.util;

import com.vanish.model.User;
import com.vanish.service.UserService;

/**
 * Seeds the admin account at startup. Credentials come from the
 * VANISH_ADMIN_USER / VANISH_ADMIN_PASSWORD environment variables, with
 * class-project defaults when unset. Override them for any real deployment.
 */
public final class AdminBootstrap {

    public static final String DEFAULT_USER = "admin";
    public static final String DEFAULT_PASSWORD = "Admin@12345";

    private AdminBootstrap() {
    }

    /** Returns the seeded admin, or null if one with that name already exists or the config is invalid. */
    public static User seed(UserService userService) {
        String user = envOr("VANISH_ADMIN_USER", DEFAULT_USER);
        String password = envOr("VANISH_ADMIN_PASSWORD", DEFAULT_PASSWORD);
        try {
            return userService.registerAdmin(user, password);
        } catch (Exception e) {
            System.err.println("[BOOTSTRAP] Admin '" + user + "' not seeded: " + e.getMessage());
            return null;
        }
    }

    private static String envOr(String key, String fallback) {
        String v = System.getenv(key);
        return v == null || v.isBlank() ? fallback : v;
    }
}
