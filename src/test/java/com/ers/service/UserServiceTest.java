package com.ers.service;

import com.ers.dao.UserDao;
import com.ers.dto.LoginRequest;
import com.ers.dto.RegisterRequest;
import com.ers.model.Department;
import com.ers.model.Role;
import com.ers.model.User;
import com.ers.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private UserService userService;

    private Department makeDept() {
        return new Department(1, "Engineering");
    }

    private RegisterRequest makeRegisterReq() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("jdoe");
        req.setPassword("password123");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setDepartmentId(1);
        return req;
    }

    @Test
    void register_ShouldSucceed_WithValidData() {
        RegisterRequest req = makeRegisterReq();
        Department dept = makeDept();

        when(userDao.findByUsername("jdoe")).thenReturn(Optional.empty());
        when(departmentService.getById(1)).thenReturn(dept);
        when(userDao.insert(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1);
            return u;
        });

        User result = userService.register(req);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("jdoe", result.getUsername());
        assertEquals(Role.EMPLOYEE, result.getRole());
        assertNotNull(result.getPassword());
        assertTrue(result.getPassword().startsWith("$2"));
        verify(userDao).insert(any(User.class));
    }

    @Test
    void register_ShouldFail_WhenUsernameTaken() {
        RegisterRequest req = makeRegisterReq();

        when(userDao.findByUsername("jdoe")).thenReturn(Optional.of(new User()));

        // departmentService.getById is never reached because username check fails first
        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test
    void register_ShouldFail_WithBlankUsername() {
        RegisterRequest req = makeRegisterReq();
        req.setUsername("  ");
        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test
    void register_ShouldFail_WithShortPassword() {
        RegisterRequest req = makeRegisterReq();
        req.setPassword("ab");
        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test
    void login_ShouldSucceed_WithCorrectCredentials() {
        Department dept = makeDept();
        String hashed = PasswordUtil.hash("password123");
        User storedUser = new User(1, "jdoe", hashed, "John", "Doe", Role.EMPLOYEE, dept);

        when(userDao.findByUsername("jdoe")).thenReturn(Optional.of(storedUser));

        LoginRequest req = new LoginRequest();
        req.setUsername("jdoe");
        req.setPassword("password123");

        User result = userService.login(req);
        assertNotNull(result);
        assertEquals("jdoe", result.getUsername());
        assertNull(result.getPassword()); // password should be cleared
    }

    @Test
    void login_ShouldFail_WithWrongPassword() {
        Department dept = makeDept();
        String hashed = PasswordUtil.hash("password123");
        User storedUser = new User(1, "jdoe", hashed, "John", "Doe", Role.EMPLOYEE, dept);

        when(userDao.findByUsername("jdoe")).thenReturn(Optional.of(storedUser));

        LoginRequest req = new LoginRequest();
        req.setUsername("jdoe");
        req.setPassword("wrongpassword");

        assertThrows(IllegalArgumentException.class, () -> userService.login(req));
    }

    @Test
    void login_ShouldFail_WhenUserNotFound() {
        when(userDao.findByUsername("unknown")).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setUsername("unknown");
        req.setPassword("password");

        assertThrows(IllegalArgumentException.class, () -> userService.login(req));
    }
}
