package com.ers.dao;

import com.ers.model.Department;
import com.ers.model.Role;
import com.ers.model.User;
import com.ers.util.DatabaseUtil;

import java.sql.*;
import java.util.Optional;

public class UserDao {

    /**
     * Inserts a new user and sets the generated id on the returned object.
     */
    public User insert(User user) {
        String sql = "INSERT INTO users (username, password, first_name, last_name, role, department_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getRole().name());
            ps.setInt(6, user.getDepartment().getId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting user", e);
        }
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT u.id, u.username, u.password, u.first_name, u.last_name, u.role, " +
                     "u.department_id, d.name AS dept_name " +
                     "FROM users u JOIN departments d ON u.department_id = d.id " +
                     "WHERE u.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        }
        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT u.id, u.username, u.password, u.first_name, u.last_name, u.role, " +
                     "u.department_id, d.name AS dept_name " +
                     "FROM users u JOIN departments d ON u.department_id = d.id " +
                     "WHERE u.username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username", e);
        }
        return Optional.empty();
    }

    private User mapRow(ResultSet rs) throws SQLException {
        Department dept = new Department(
                rs.getInt("department_id"),
                rs.getString("dept_name")
        );
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                Role.valueOf(rs.getString("role")),
                dept
        );
    }
}
