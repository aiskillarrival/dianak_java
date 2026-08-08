package com.ers.service;

import com.ers.dao.ReimbursementDao;
import com.ers.dto.ReimbursementRequest;
import com.ers.model.*;

import java.util.List;

public class ReimbursementService {

    private final ReimbursementDao reimbursementDao;
    private final UserService userService;

    public ReimbursementService(ReimbursementDao reimbursementDao, UserService userService) {
        this.reimbursementDao = reimbursementDao;
        this.userService = userService;
    }

    /**
     * Submits a new reimbursement request for the given employee.
     */
    public Reimbursement submit(int authorId, ReimbursementRequest req) {
        if (req.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (req.getDescription() == null || req.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        ReimbursementType type;
        try {
            type = ReimbursementType.valueOf(req.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type. Must be TRAVEL, FOOD, LODGING, or OTHER");
        }

        User author = userService.getById(authorId);

        Reimbursement r = new Reimbursement();
        r.setAmount(req.getAmount());
        r.setDescription(req.getDescription());
        r.setType(type);
        r.setStatus(ReimbursementStatus.PENDING);
        r.setAuthor(author);

        reimbursementDao.insert(r);
        return reimbursementDao.findById(r.getId()).orElseThrow();
    }

    /**
     * Edits a PENDING reimbursement. Only the author can edit.
     */
    public Reimbursement update(int reimbursementId, int userId, ReimbursementRequest req) {
        Reimbursement existing = reimbursementDao.findById(reimbursementId)
                .orElseThrow(() -> new IllegalArgumentException("Reimbursement not found"));

        if (existing.getAuthor().getId() != userId) {
            throw new IllegalArgumentException("You can only edit your own reimbursements");
        }
        if (existing.getStatus() != ReimbursementStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING reimbursements can be edited");
        }

        if (req.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (req.getDescription() == null || req.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        ReimbursementType type;
        try {
            type = ReimbursementType.valueOf(req.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type. Must be TRAVEL, FOOD, LODGING, or OTHER");
        }

        existing.setAmount(req.getAmount());
        existing.setDescription(req.getDescription());
        existing.setType(type);

        reimbursementDao.update(existing);
        return reimbursementDao.findById(reimbursementId).orElseThrow();
    }

    /**
     * Resolves (approves/denies) a reimbursement. Only managers can resolve.
     */
    public Reimbursement resolve(int reimbursementId, int managerId, ReimbursementStatus status) {
        User manager = userService.getById(managerId);
        if (manager.getRole() != Role.MANAGER) {
            throw new IllegalArgumentException("Only managers can resolve reimbursements");
        }

        Reimbursement existing = reimbursementDao.findById(reimbursementId)
                .orElseThrow(() -> new IllegalArgumentException("Reimbursement not found"));

        if (existing.getStatus() != ReimbursementStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING reimbursements can be resolved");
        }

        reimbursementDao.resolve(reimbursementId, status, managerId);
        return reimbursementDao.findById(reimbursementId).orElseThrow();
    }

    /**
     * Returns reimbursements for the given author, optionally filtered by status.
     */
    public List<Reimbursement> getByAuthor(int authorId, String statusFilter) {
        return reimbursementDao.findByAuthor(authorId, statusFilter);
    }

    /**
     * Returns all reimbursements (for managers), optionally filtered by status and department.
     */
    public List<Reimbursement> getAll(String statusFilter, Integer departmentId) {
        return reimbursementDao.findAll(statusFilter, departmentId);
    }

    public Reimbursement getById(int id) {
        return reimbursementDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reimbursement not found: " + id));
    }
}
