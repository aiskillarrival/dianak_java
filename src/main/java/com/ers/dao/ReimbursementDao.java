package com.ers.dao;

import com.ers.model.*;
import com.ers.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReimbursementDao {

    /**
     * Inserts a new reimbursement and sets the generated id.
     */
    public Reimbursement insert(Reimbursement r) {
        String sql = "INSERT INTO reimbursements (amount, description, type, status, author_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, r.getAmount());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getType().name());
            ps.setString(4, ReimbursementStatus.PENDING.name());
            ps.setInt(5, r.getAuthor().getId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getInt(1));
                }
            }
            return r;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting reimbursement", e);
        }
    }

    /**
     * Updates the amount, description, and type of an existing PENDING reimbursement.
     */
    public boolean update(Reimbursement r) {
        String sql = "UPDATE reimbursements SET amount = ?, description = ?, type = ? " +
                     "WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, r.getAmount());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getType().name());
            ps.setInt(4, r.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating reimbursement", e);
        }
    }

    /**
     * Resolves a reimbursement (approve/deny), recording the resolver and timestamp.
     */
    public boolean resolve(int id, ReimbursementStatus status, int resolverId) {
        String sql = "UPDATE reimbursements SET status = ?, resolver_id = ?, " +
                     "resolved_at = CURRENT_TIMESTAMP WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, resolverId);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error resolving reimbursement", e);
        }
    }

    public Optional<Reimbursement> findById(int id) {
        String sql = buildSelectQuery() + " WHERE r.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reimbursement by id", e);
        }
        return Optional.empty();
    }

    /**
     * Returns reimbursements for a specific author, optionally filtered by status.
     */
    public List<Reimbursement> findByAuthor(int authorId, String statusFilter) {
        StringBuilder sql = new StringBuilder(buildSelectQuery());
        sql.append(" WHERE r.author_id = ?");
        if (statusFilter != null && !statusFilter.isBlank()) {
            sql.append(" AND r.status = ?");
        }
        sql.append(" ORDER BY r.submitted_at DESC");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, authorId);
            if (statusFilter != null && !statusFilter.isBlank()) {
                ps.setString(2, statusFilter.toUpperCase());
            }
            List<Reimbursement> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reimbursements by author", e);
        }
    }

    /**
     * Returns all reimbursements, optionally filtered by status and/or department.
     */
    public List<Reimbursement> findAll(String statusFilter, Integer departmentId) {
        StringBuilder sql = new StringBuilder(buildSelectQuery());
        sql.append(" WHERE 1=1");
        if (statusFilter != null && !statusFilter.isBlank()) {
            sql.append(" AND r.status = ?");
        }
        if (departmentId != null) {
            sql.append(" AND u.department_id = ?");
        }
        sql.append(" ORDER BY r.submitted_at DESC");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (statusFilter != null && !statusFilter.isBlank()) {
                ps.setString(idx++, statusFilter.toUpperCase());
            }
            if (departmentId != null) {
                ps.setInt(idx, departmentId);
            }
            List<Reimbursement> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all reimbursements", e);
        }
    }

    private String buildSelectQuery() {
        return "SELECT r.id, r.amount, r.description, r.type, r.status, " +
               "r.submitted_at, r.resolved_at, " +
               "r.author_id, au.username AS author_username, " +
               "au.first_name AS author_first, au.last_name AS author_last, " +
               "au.role AS author_role, " +
               "au.department_id AS author_dept_id, ad.name AS author_dept_name, " +
               "r.resolver_id, ru.username AS resolver_username, " +
               "ru.first_name AS resolver_first, ru.last_name AS resolver_last, " +
               "ru.role AS resolver_role, " +
               "ru.department_id AS resolver_dept_id, rd.name AS resolver_dept_name " +
               "FROM reimbursements r " +
               "JOIN users au ON r.author_id = au.id " +
               "JOIN departments ad ON au.department_id = ad.id " +
               "LEFT JOIN users ru ON r.resolver_id = ru.id " +
               "LEFT JOIN departments rd ON ru.department_id = rd.id";
    }

    private Reimbursement mapRow(ResultSet rs) throws SQLException {
        Department authorDept = new Department(
                rs.getInt("author_dept_id"), rs.getString("author_dept_name"));

        User author = new User(
                rs.getInt("author_id"),
                rs.getString("author_username"),
                null, // don't expose password
                rs.getString("author_first"),
                rs.getString("author_last"),
                Role.valueOf(rs.getString("author_role")),
                authorDept
        );

        User resolver = null;
        int resolverId = rs.getInt("resolver_id");
        if (!rs.wasNull()) {
            Department resolverDept = new Department(
                    rs.getInt("resolver_dept_id"), rs.getString("resolver_dept_name"));
            resolver = new User(
                    resolverId,
                    rs.getString("resolver_username"),
                    null,
                    rs.getString("resolver_first"),
                    rs.getString("resolver_last"),
                    Role.valueOf(rs.getString("resolver_role")),
                    resolverDept
            );
        }

        Timestamp submittedTs = rs.getTimestamp("submitted_at");
        Timestamp resolvedTs = rs.getTimestamp("resolved_at");

        return new Reimbursement(
                rs.getInt("id"),
                rs.getDouble("amount"),
                rs.getString("description"),
                ReimbursementType.valueOf(rs.getString("type")),
                ReimbursementStatus.valueOf(rs.getString("status")),
                author,
                resolver,
                submittedTs != null ? submittedTs.toLocalDateTime() : null,
                resolvedTs != null ? resolvedTs.toLocalDateTime() : null
        );
    }
}
