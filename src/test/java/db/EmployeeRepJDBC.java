package db;

import model.Employee;
import utils.ConfigLoader;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepJDBC implements EmployeeRep {

    private static final String SQL_INSERT_EMPLOYEE = "INSERT INTO employee (first_name, last_name, phone, email, company_id) VALUES (?, ?, ?, ?, ?) RETURNING id";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM employee WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM employee WHERE id = ?";

    @Override
    public int createEmployeeDB(Employee employee) throws SQLException {
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_EMPLOYEE)) {

            statement.setString(1, employee.getFirstName());
            statement.setString(2, employee.getLastName());
            statement.setString(3, employee.getPhone());
            statement.setString(4, employee.getEmail());
            statement.setInt(5, employee.getCompanyId());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                throw new SQLException("Failed to get generated key.");
            }
        }
    }

    @Override
    public Employee getEmployeeByIdDB(int id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToEmployee(resultSet);
                } else {
                    throw new SQLException("Employee with id " + id + " not found.");
                }
            }
        }
    }

    @Override
    public void deleteEmployeeByIdDB(int id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public int createEmployee(Employee employee) throws SQLException {
        return 0;
    }

    @Override
    public Employee getEmployeeById(int id) throws SQLException {
        return null;
    }

    @Override
    public void deleteEmployeeById(int id) throws SQLException {

    }

    private Employee mapResultSetToEmployee(ResultSet resultSet) throws SQLException {
        Employee employee = new Employee();
        employee.setId(resultSet.getInt("id"));
        employee.setFirstName(resultSet.getString("first_name"));
        employee.setMiddleName(resultSet.getString("middle_name"));
        employee.setLastName(resultSet.getString("last_name"));
        employee.setCompanyId(resultSet.getInt("company_id"));
        employee.setEmail(resultSet.getString("email"));
        employee.setPhone(resultSet.getString("phone"));

        // Преобразование java.sql.Date в java.util.Date для поля birthdate
        Date sqlDate = resultSet.getDate("birthdate");
        java.util.Date utilDate = new java.util.Date(sqlDate.getTime());

        // Преобразование java.util.Date в строку
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(utilDate);

        employee.setBirthdate(dateString); // Здесь передаем строку

        employee.setActive(resultSet.getBoolean("is_active"));
        return employee;
    }


    // Additional methods as per requirements (assuming methods for filtering and checking)

    @Override
    public boolean isEmployeeActive(int id) throws SQLException {
        String sql = "SELECT is_active FROM employee WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("is_active");
                } else {
                    throw new SQLException("Employee with id " + id + " not found.");
                }
            }
        }
    }
    public List<Employee> getEmployeesByCompany(int companyId) {
        String sql = "SELECT * FROM employee WHERE company_id = ? AND is_active = true";
        List<Employee> employees = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, companyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Employee employee = new Employee();
                    employee.setId(resultSet.getInt("id"));
                    employee.setFirstName(resultSet.getString("first_name"));
                    // Set other employee fields
                    employees.add(employee);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving employees: " + e.getMessage());
        }
        return employees;
    }

    @Override
    public boolean isEmployeeInNonExistentCompany(int companyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM company WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, companyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count == 0;
                } else {
                    throw new SQLException("Error checking if company exists.");
                }
            }
        }
    }

    @Override
    public List<Employee> getActiveEmployees() throws SQLException {
        List<Employee> activeEmployees = new ArrayList<>();
        String sql = "SELECT * FROM employee WHERE is_active = ?";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, true);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    activeEmployees.add(mapResultSetToEmployee(resultSet));
                }
            }
        }
        return activeEmployees;
    }
}
