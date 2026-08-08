package com.ers.model;

import java.time.LocalDateTime;

public class Reimbursement {
    private int id;
    private double amount;
    private String description;
    private ReimbursementType type;
    private ReimbursementStatus status;
    private User author;
    private User resolver;
    private LocalDateTime submittedAt;
    private LocalDateTime resolvedAt;

    public Reimbursement() {}

    public Reimbursement(int id, double amount, String description, ReimbursementType type,
                         ReimbursementStatus status, User author, User resolver,
                         LocalDateTime submittedAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.status = status;
        this.author = author;
        this.resolver = resolver;
        this.submittedAt = submittedAt;
        this.resolvedAt = resolvedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ReimbursementType getType() { return type; }
    public void setType(ReimbursementType type) { this.type = type; }

    public ReimbursementStatus getStatus() { return status; }
    public void setStatus(ReimbursementStatus status) { this.status = status; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public User getResolver() { return resolver; }
    public void setResolver(User resolver) { this.resolver = resolver; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    @Override
    public String toString() {
        return "Reimbursement{id=" + id + ", amount=" + amount + ", type=" + type +
               ", status=" + status + "}";
    }
}
