package com.ers.dao;

import com.ers.model.Department;
import com.ers.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentDao {

    public Optional<Department> findById(int id) {
        String sql = "SELECT id, name FROM departments WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding department by id", e);
        }
        return Optional.empty();
    }

    public Optional<Department> findByName(String name) {
        String sql = "SELECT id, name FROM departments WHERE name = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding department by name", e);
        }
        return Optional.empty();
    }

    public List<Department> findAll() {
        String sql = "SELECT id, name FROM departments ORDER BY name";
        List<Department> departments = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                departments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all departments", e);
        }
        return departments;
    }

    private Department mapRow(ResultSet rs) throws SQLException {
        return new Department(rs.getInt("id"), rs.getString("name"));
    }
}
