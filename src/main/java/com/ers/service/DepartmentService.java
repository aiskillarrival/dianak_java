package com.ers.service;

import com.ers.dao.DepartmentDao;
import com.ers.model.Department;

import java.util.List;

public class DepartmentService {

    private final DepartmentDao departmentDao;

    public DepartmentService(DepartmentDao departmentDao) {
        this.departmentDao = departmentDao;
    }

    public List<Department> getAllDepartments() {
        return departmentDao.findAll();
    }

    public Department getById(int id) {
        return departmentDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
    }
}
