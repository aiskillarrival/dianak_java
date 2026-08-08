package com.ers.controller;

import com.ers.model.Department;
import com.ers.service.DepartmentService;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * GET /api/departments — List all departments.
     */
    public void list(Context ctx) {
        List<Department> departments = departmentService.getAllDepartments();
        List<Map<String, Object>> result = departments.stream()
                .map(d -> Map.<String, Object>of("id", d.getId(), "name", d.getName()))
                .toList();
        ctx.status(200).json(result);
    }
}
