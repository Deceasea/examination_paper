package db;

import model.Employee;

import java.sql.SQLException;
import java.util.List;

public interface EmployeeRep {

    /**
     * Creates an employee in the database.
     *
     * @param employee The employee to add.
     * @return The ID of the new employee.
     * @throws SQLException If there is an error working with the database.
     */
    @io.qameta.allure.Step("Creating an employee in the database")
    int createEmployeeDB(Employee employee) throws SQLException;

    /**
     * Retrieves an employee from the database by their ID.
     *
     * @param id The ID of the employee.
     * @return The employee with the specified ID.
     * @throws SQLException If there is an error working with the database.
     */
    @io.qameta.allure.Step("Retrieving an employee by ID from the database")
    Employee getEmployeeByIdDB(int id) throws SQLException;

    /**
     * Deletes an employee from the database by their ID.
     *
     * @param id The ID of the employee.
     * @throws SQLException If there is an error working with the database.
     */
    @io.qameta.allure.Step("Deleting an employee by ID from the database")
    void deleteEmployeeByIdDB(int id) throws SQLException;

    /**
     * Creates an employee.
     *
     * @param employee The employee to add.
     * @return The ID of the new employee.
     * @throws SQLException If there is an error working with the database.
     */
    @io.qameta.allure.Step("Creating an employee")
    int createEmployee(Employee employee) throws SQLException;

    /**
     * Retrieves an employee by their ID.
     *
     * @param id The ID of the employee.
     * @return The employee with the specified ID.
     * @throws SQLException If there is an error working with the database.
     */
    @io.qameta.allure.Step("Retrieving an employee by ID")
    Employee getEmployeeById(int id) throws SQLException;

    /**
     * Deletes an employee by their ID.
     *
     * @param id The ID of the employee.
     * @throws SQLException If there is an error working with the database.
     */
    @io.qameta.allure.Step("Deleting an employee by ID")
    void deleteEmployeeById(int id) throws SQLException;

    boolean isEmployeeActive(int id) throws SQLException;

    boolean isEmployeeInNonExistentCompany(int companyId) throws SQLException;

    List<Employee> getActiveEmployees() throws SQLException;
}
