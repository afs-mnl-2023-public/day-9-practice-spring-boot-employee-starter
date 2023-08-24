package com.afs.restapi.repository;

import com.afs.restapi.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeJpaRepository extends JpaRepository<Employee,Long> {
    List<Employee> findAllByGender(String gender);
}