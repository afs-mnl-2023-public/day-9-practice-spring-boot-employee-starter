package com.afs.restapi;

import com.afs.restapi.entity.Company;
import com.afs.restapi.entity.Employee;
import com.afs.restapi.repository.CompanyJpaRepository;
import com.afs.restapi.repository.EmployeeJpaRepository;
import com.afs.restapi.repository.InMemoryCompanyRepository;
import com.afs.restapi.repository.InMemoryEmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryCompanyRepository inMemoryCompanyRepository;

    @Autowired
    private InMemoryEmployeeRepository inMemoryEmployeeRepository;

    @Autowired
    private CompanyJpaRepository companyJpaRepository;
    @Autowired
    private EmployeeJpaRepository employeeJpaRepository;

    @BeforeEach
    void setUp() {
        inMemoryCompanyRepository.clearAll();
        inMemoryEmployeeRepository.clearAll();
        companyJpaRepository.deleteAll();
        employeeJpaRepository.deleteAll();
    }

    @Test
    void should_update_company_name() throws Exception {
        Company previousCompany = new Company(1L, "abc");
        Company savedCompany = companyJpaRepository.save(previousCompany);

        Company companyUpdateRequest = new Company(1L, "xyz");
        ObjectMapper objectMapper = new ObjectMapper();
        String updatedCompanyJson = objectMapper.writeValueAsString(companyUpdateRequest);
        mockMvc.perform(put("/companies/{id}", savedCompany.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedCompanyJson))
                .andExpect(MockMvcResultMatchers.status().is(204));

        Optional<Company> optionalCompany = companyJpaRepository.findById(savedCompany.getId());
        assertTrue(optionalCompany.isPresent());
        Company updatedCompany = optionalCompany.get();
        Assertions.assertEquals(savedCompany.getId(), updatedCompany.getId());
        Assertions.assertEquals(companyUpdateRequest.getName(), updatedCompany.getName());
    }

    @Test
    void should_delete_company_name() throws Exception {
        Company company = new Company(1L, "abc");
        Company savedCompany = companyJpaRepository.save(company);

        mockMvc.perform(delete("/companies/{id}", savedCompany.getId()))
                .andExpect(MockMvcResultMatchers.status().is(204));

        assertTrue(companyJpaRepository.findById(savedCompany.getId()).isEmpty());
    }

    @Test
    void should_create_employee() throws Exception {
        Company newCompany = companyJpaRepository.save(getCompany1());

        ObjectMapper objectMapper = new ObjectMapper();
        String companyRequest = objectMapper.writeValueAsString(newCompany);
        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyRequest))
                .andExpect(MockMvcResultMatchers.status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(newCompany.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(newCompany.getName()));
    }

    @Test
    void should_find_companies() throws Exception {
        Company existingCompany = companyJpaRepository.save(getCompany1());

        mockMvc.perform(get("/companies"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(existingCompany.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(existingCompany.getName()));
    }

    @Test
    void should_find_companies_by_page() throws Exception {
        Company company1 = companyJpaRepository.save(getCompany1());
        Company company2 = companyJpaRepository.save(getCompany1());
        Company company3 = companyJpaRepository.save(getCompany1());

        mockMvc.perform(get("/companies")
                        .param("pageNumber", "0")
                        .param("pageSize", "2"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(company1.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(company1.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(company2.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(company2.getName()))
        ;
    }

    @Test
    void should_find_company_by_id() throws Exception {
        Company company = companyJpaRepository.save(getCompany1());
        Employee newEmployee = new Employee(99L,"Peter Gulliver", 69, "Male", 420069);
        company.getEmployees().add(newEmployee);
        Employee employee = employeeJpaRepository.save(getEmployee(company));


        mockMvc.perform(get("/companies/{id}", company.getId()))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(company.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(company.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].id").value(employee.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].name").value(employee.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].age").value(employee.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].gender").value(employee.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employees[0].salary").value(employee.getSalary()));
    }

    @Test
    void should_find_employees_by_companies() throws Exception {
        Company company = getCompany1();
        inMemoryCompanyRepository.insert(company);
        Employee employee = getEmployee(company);
        inMemoryEmployeeRepository.insert(employee);

        mockMvc.perform(get("/companies/{companyId}/employees", 1L))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(employee.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(employee.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].age").value(employee.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].gender").value(employee.getGender()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].salary").value(employee.getSalary()));
    }

    private static Employee getEmployee(Company company) {
        Employee employee = new Employee();
        employee.setName("Bob");
        employee.setAge(22);
        employee.setGender("Male");
        employee.setSalary(10000);
        employee.setCompanyId(company.getId());
        return employee;
    }


    private static Company getCompany1() {
        List<Employee> employees = new ArrayList<>();
        Company company = new Company();
        company.setName("ABC");
        company.setEmployees(employees);
        return company;
    }

    private static Company getCompany2() {
        Company company = new Company();
        company.setName("DEF");
        return company;
    }

    private static Company getCompany3() {
        Company company = new Company();
        company.setName("XYZ");
        return company;
    }
}