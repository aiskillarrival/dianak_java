package com.ers.service;

import com.ers.dao.ReimbursementDao;
import com.ers.dto.ReimbursementRequest;
import com.ers.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReimbursementServiceTest {

    @Mock
    private ReimbursementDao reimbursementDao;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReimbursementService reimbursementService;

    private User makeEmployee() {
        Department dept = new Department(1, "Engineering");
        return new User(1, "jdoe", null, "John", "Doe", Role.EMPLOYEE, dept);
    }

    private User makeManager() {
        Department dept = new Department(1, "Engineering");
        return new User(2, "manager", null, "Jane", "Smith", Role.MANAGER, dept);
    }

    private ReimbursementRequest makeReimbReq() {
        ReimbursementRequest req = new ReimbursementRequest();
        req.setAmount(120.00);
        req.setDescription("Client dinner");
        req.setType("FOOD");
        return req;
    }

    private Reimbursement makePendingReimb(User author) {
        return new Reimbursement(1, 120.00, "Client dinner", ReimbursementType.FOOD,
                ReimbursementStatus.PENDING, author, null,
                LocalDateTime.now(), null);
    }

    @Test
    void submit_ShouldSucceed_WithValidData() {
        User employee = makeEmployee();
        ReimbursementRequest req = makeReimbReq();

        when(userService.getById(1)).thenReturn(employee);
        when(reimbursementDao.insert(any(Reimbursement.class))).thenAnswer(inv -> {
            Reimbursement r = inv.getArgument(0);
            r.setId(1);
            return r;
        });

        Reimbursement result = reimbursementService.submit(1, req);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(120.00, result.getAmount());
        assertEquals(ReimbursementType.FOOD, result.getType());
        assertEquals(ReimbursementStatus.PENDING, result.getStatus());
        assertEquals(employee, result.getAuthor());
    }

    @Test
    void submit_ShouldFail_WithNegativeAmount() {
        ReimbursementRequest req = makeReimbReq();
        req.setAmount(-50);
        assertThrows(IllegalArgumentException.class, () -> reimbursementService.submit(1, req));
    }

    @Test
    void submit_ShouldFail_WithBlankDescription() {
        ReimbursementRequest req = makeReimbReq();
        req.setDescription("  ");
        assertThrows(IllegalArgumentException.class, () -> reimbursementService.submit(1, req));
    }

    @Test
    void submit_ShouldFail_WithInvalidType() {
        ReimbursementRequest req = makeReimbReq();
        req.setType("INVALID");
        assertThrows(IllegalArgumentException.class, () -> reimbursementService.submit(1, req));
    }

    @Test
    void update_ShouldSucceed_WhenAuthorEditsPending() {
        User employee = makeEmployee();
        Reimbursement existing = makePendingReimb(employee);
        ReimbursementRequest req = makeReimbReq();
        req.setAmount(200.00);
        req.setType("TRAVEL");

        when(reimbursementDao.findById(1)).thenReturn(Optional.of(existing));
        when(reimbursementDao.update(any(Reimbursement.class))).thenReturn(true);
        when(reimbursementDao.findById(1)).thenReturn(Optional.of(existing)); // after update

        Reimbursement result = reimbursementService.update(1, 1, req);

        assertNotNull(result);
        assertEquals(200.00, result.getAmount());
        assertEquals(ReimbursementType.TRAVEL, result.getType());
    }

    @Test
    void update_ShouldFail_WhenNotAuthor() {
        User employee = makeEmployee();
        Reimbursement existing = makePendingReimb(employee);
        ReimbursementRequest req = makeReimbReq();

        when(reimbursementDao.findById(1)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> reimbursementService.update(1, 999, req));
    }

    @Test
    void update_ShouldFail_WhenNotPending() {
        User employee = makeEmployee();
        Reimbursement approved = new Reimbursement(1, 120.00, "Client dinner",
                ReimbursementType.FOOD, ReimbursementStatus.APPROVED, employee, null,
                LocalDateTime.now(), null);
        ReimbursementRequest req = makeReimbReq();

        when(reimbursementDao.findById(1)).thenReturn(Optional.of(approved));

        assertThrows(IllegalArgumentException.class,
                () -> reimbursementService.update(1, 1, req));
    }

    @Test
    void resolve_ShouldSucceed_WhenManagerResolvesPending() {
        User manager = makeManager();
        User employee = makeEmployee();
        Reimbursement existing = makePendingReimb(employee);

        when(userService.getById(2)).thenReturn(manager);
        when(reimbursementDao.findById(1)).thenReturn(Optional.of(existing));
        when(reimbursementDao.resolve(1, ReimbursementStatus.APPROVED, 2)).thenReturn(true);
        when(reimbursementDao.findById(1)).thenReturn(Optional.of(existing));

        Reimbursement result = reimbursementService.resolve(1, 2, ReimbursementStatus.APPROVED);

        assertNotNull(result);
        verify(reimbursementDao).resolve(1, ReimbursementStatus.APPROVED, 2);
    }

    @Test
    void resolve_ShouldFail_WhenNotManager() {
        User employee = makeEmployee();

        when(userService.getById(1)).thenReturn(employee);

        assertThrows(IllegalArgumentException.class,
                () -> reimbursementService.resolve(1, 1, ReimbursementStatus.APPROVED));
    }

    @Test
    void resolve_ShouldFail_WhenAlreadyResolved() {
        User manager = makeManager();
        User employee = makeEmployee();
        Reimbursement approved = new Reimbursement(1, 120.00, "Client dinner",
                ReimbursementType.FOOD, ReimbursementStatus.APPROVED, employee, manager,
                LocalDateTime.now(), LocalDateTime.now());

        when(userService.getById(2)).thenReturn(manager);
        when(reimbursementDao.findById(1)).thenReturn(Optional.of(approved));

        assertThrows(IllegalArgumentException.class,
                () -> reimbursementService.resolve(1, 2, ReimbursementStatus.APPROVED));
    }
}
