package com.ers.service;

import com.ers.dao.UserDao;
import com.ers.dto.LoginRequest;
import com.ers.dto.RegisterRequest;
import com.ers.model.Department;
import com.ers.model.Role;
import com.ers.model.User;
import com.ers.util.PasswordUtil;

public class UserService {

    private final UserDao userDao;
    private final DepartmentService departmentService;

    public UserService(UserDao userDao, DepartmentService departmentService) {
        this.userDao = userDao;
        this.departmentService = departmentService;
    }

    /**
     * Registers a new user with default role EMPLOYEE.
     */
    public User register(RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (req.getPassword() == null || req.getPassword().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
        if (req.getFirstName() == null || req.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (req.getLastName() == null || req.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (userDao.findByUsername(req.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }

        Department dept = departmentService.getById(req.getDepartmentId());

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(PasswordUtil.hash(req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setRole(Role.EMPLOYEE);
        user.setDepartment(dept);

        return userDao.insert(user);
    }

    /**
     * Authenticates a user by username and password.
     * Returns the user (without password) on success.
     */
    public User login(LoginRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        User user = userDao.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!PasswordUtil.verify(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // Clear password before returning
        user.setPassword(null);
        return user;
    }

    public User getById(int id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setPassword(null);
        return user;
    }
}
