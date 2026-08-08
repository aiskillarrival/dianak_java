package com.ers.controller;

import com.ers.dto.ReimbursementRequest;
import com.ers.model.Reimbursement;
import com.ers.model.ReimbursementStatus;
import com.ers.model.Role;
import com.ers.model.User;
import com.ers.service.ReimbursementService;
import com.ers.service.UserService;
import com.google.gson.Gson;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ReimbursementController {

    private static final Logger log = LoggerFactory.getLogger(ReimbursementController.class);

    private final ReimbursementService reimbursementService;
    private final UserService userService;
    private final Gson gson;

    public ReimbursementController(ReimbursementService reimbursementService,
                                   UserService userService, Gson gson) {
        this.reimbursementService = reimbursementService;
        this.userService = userService;
        this.gson = gson;
    }

    /**
     * POST /api/reimbursements — Submit a new reimbursement.
     */
    public void submit(Context ctx) {
        int userId = AuthController.requireAuth(ctx);
        if (userId == -1) return;

        try {
            ReimbursementRequest req = gson.fromJson(ctx.body(), ReimbursementRequest.class);
            Reimbursement r = reimbursementService.submit(userId, req);
            log.info("Reimbursement submitted: id={} by userId={}", r.getId(), userId);
            ctx.status(201).json(toJson(r));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/reimbursements/{id} — Edit a PENDING reimbursement.
     */
    public void update(Context ctx) {
        int userId = AuthController.requireAuth(ctx);
        if (userId == -1) return;

        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            ReimbursementRequest req = gson.fromJson(ctx.body(), ReimbursementRequest.class);
            Reimbursement r = reimbursementService.update(id, userId, req);
            ctx.status(200).json(toJson(r));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "Invalid reimbursement id"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/reimbursements/{id}/resolve — Approve or deny a reimbursement (manager only).
     */
    public void resolve(Context ctx) {
        int userId = AuthController.requireAuth(ctx);
        if (userId == -1) return;

        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<String, String> body = gson.fromJson(ctx.body(), Map.class);
            String statusStr = body.get("status");
            if (statusStr == null) {
                ctx.status(400).json(Map.of("error", "Status is required (APPROVED or DENIED)"));
                return;
            }
            ReimbursementStatus status = ReimbursementStatus.valueOf(statusStr.toUpperCase());
            Reimbursement r = reimbursementService.resolve(id, userId, status);
            log.info("Reimbursement {} resolved as {} by managerId={}", id, status, userId);
            ctx.status(200).json(toJson(r));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "Invalid reimbursement id"));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg.contains("No enum constant")) {
                ctx.status(400).json(Map.of("error", "Status must be APPROVED or DENIED"));
            } else {
                ctx.status(400).json(Map.of("error", msg));
            }
        }
    }

    /**
     * GET /api/reimbursements — View reimbursements.
     * Employees see their own; managers see all.
     * Query params: status, departmentId (managers only).
     */
    public void list(Context ctx) {
        int userId = AuthController.requireAuth(ctx);
        if (userId == -1) return;

        String statusFilter = ctx.queryParam("status");
        String deptParam = ctx.queryParam("departmentId");

        try {
            User user = userService.getById(userId);
            List<Reimbursement> list;

            if (user.getRole() == Role.MANAGER) {
                Integer deptId = (deptParam != null && !deptParam.isBlank())
                        ? Integer.parseInt(deptParam) : null;
                list = reimbursementService.getAll(statusFilter, deptId);
            } else {
                // Employees see only their own
                list = reimbursementService.getByAuthor(userId, statusFilter);
            }

            ctx.status(200).json(list.stream().map(this::toJson).toList());
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/reimbursements/{id} — Get a single reimbursement by id.
     */
    public void getById(Context ctx) {
        int userId = AuthController.requireAuth(ctx);
        if (userId == -1) return;

        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Reimbursement r = reimbursementService.getById(id);

            // Employees can only view their own; managers can view all
            User user = userService.getById(userId);
            if (user.getRole() != Role.MANAGER && r.getAuthor().getId() != userId) {
                ctx.status(403).json(Map.of("error", "Access denied"));
                return;
            }

            ctx.status(200).json(toJson(r));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "Invalid reimbursement id"));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> toJson(Reimbursement r) {
        Map<String, Object> json = new java.util.LinkedHashMap<>();
        json.put("id", r.getId());
        json.put("amount", r.getAmount());
        json.put("description", r.getDescription());
        json.put("type", r.getType().name());
        json.put("status", r.getStatus().name());
        json.put("submittedAt", r.getSubmittedAt() != null ? r.getSubmittedAt().toString() : null);
        json.put("resolvedAt", r.getResolvedAt() != null ? r.getResolvedAt().toString() : null);

        json.put("author", Map.of(
                "id", r.getAuthor().getId(),
                "username", r.getAuthor().getUsername(),
                "firstName", r.getAuthor().getFirstName(),
                "lastName", r.getAuthor().getLastName()
        ));

        if (r.getResolver() != null) {
            json.put("resolver", Map.of(
                    "id", r.getResolver().getId(),
                    "username", r.getResolver().getUsername(),
                    "firstName", r.getResolver().getFirstName(),
                    "lastName", r.getResolver().getLastName()
            ));
        } else {
            json.put("resolver", null);
        }

        return json;
    }
}
