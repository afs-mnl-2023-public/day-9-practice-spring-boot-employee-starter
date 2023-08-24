package com.afs.restapi.service;

import com.afs.restapi.entity.Company;
import com.afs.restapi.entity.Employee;
import com.afs.restapi.exception.CompanyNotFoundException;
import com.afs.restapi.repository.CompanyJpaRepository;
import com.afs.restapi.repository.EmployeeJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyJpaRepository companyJpaRepository;

    private final EmployeeJpaRepository employeeJpaRepository;

    public CompanyService(CompanyJpaRepository companyJpaRepository, EmployeeJpaRepository employeeJpaRepository) {
        this.companyJpaRepository = companyJpaRepository;
        this.employeeJpaRepository = employeeJpaRepository;
    }

    public List<Company> findAll() {
        return companyJpaRepository.findAll();
    }

    public List<Company> findByPage(Integer pageNumber, Integer pageSize) {
        return companyJpaRepository.findAll(PageRequest.of(pageNumber, pageSize)).toList();
    }

    public Company findById(Long id) {
        return companyJpaRepository.findById(id).orElseThrow(CompanyNotFoundException::new);
    }

    public void update(Long id, Company company) {
        Company toBeUpdatedCompany = findById(id);
        toBeUpdatedCompany.setName(company.getName());
        companyJpaRepository.save(toBeUpdatedCompany);
    }

    public Company create(Company company) {
        return companyJpaRepository.save(company);
    }

    public List<Employee> findEmployeesByCompanyId(Long id) {
        return employeeJpaRepository.findByCompanyId(id);
    }

    public void delete(Long id) {
        companyJpaRepository.deleteById(id);
    }
}
