package com.ers.controller;

import com.ers.dto.LoginRequest;
import com.ers.dto.RegisterRequest;
import com.ers.model.User;
import com.ers.service.UserService;
import com.google.gson.Gson;
import io.javalin.http.Context;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    // Simple in-memory session store: token -> userId
    private static final Map<String, Integer> sessions = new ConcurrentHashMap<>();

    private final UserService userService;
    private final Gson gson;

    public AuthController(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }

    /**
     * Extracts the userId from a session token in the Authorization header.
     * Returns -1 if not authenticated.
     */
    public static int getAuthenticatedUserId(Context ctx) {
        String authHeader = ctx.header("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return -1;
        }
        String token = authHeader.substring(7);
        return sessions.getOrDefault(token, -1);
    }

    /**
     * Requires authentication. Sends 401 if not authenticated.
     * Returns the authenticated userId.
     */
    public static int requireAuth(Context ctx) {
        int userId = getAuthenticatedUserId(ctx);
        if (userId == -1) {
            ctx.status(401).json(Map.of("error", "Authentication required"));
        }
        return userId;
    }

    public void register(Context ctx) {
        try {
            RegisterRequest req = gson.fromJson(ctx.body(), RegisterRequest.class);
            User user = userService.register(req);
            ctx.status(201).json(toUserJson(user));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    public void login(Context ctx) {
        try {
            LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);
            User user = userService.login(req);

            String token = UUID.randomUUID().toString();
            sessions.put(token, user.getId());

            ctx.status(200).json(Map.of(
                    "token", token,
                    "user", toUserJson(user)
            ));
            log.info("User logged in: {} (id={})", user.getUsername(), user.getId());
        } catch (IllegalArgumentException e) {
            ctx.status(401).json(Map.of("error", e.getMessage()));
        }
    }

    public void logout(Context ctx) {
        String authHeader = ctx.header("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            sessions.remove(token);
        }
        ctx.status(200).json(Map.of("message", "Logged out"));
    }

    public void me(Context ctx) {
        int userId = requireAuth(ctx);
        if (userId == -1) return;

        try {
            User user = userService.getById(userId);
            ctx.status(200).json(toUserJson(user));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> toUserJson(User user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "role", user.getRole().name(),
                "department", Map.of(
                        "id", user.getDepartment().getId(),
                        "name", user.getDepartment().getName()
                )
        );
    }
}
