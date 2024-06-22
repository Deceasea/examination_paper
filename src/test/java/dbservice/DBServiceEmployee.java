package dbservice;

import io.qameta.allure.Step;
import utils.ConfigLoader;

import java.sql.*;

public class DBServiceEmployee {

    // Method to add a new employee
    @Step("Adding a new employee")
    public static void addEmployee(String firstName, String middleName, String lastName, String phone, String email,
                                   Date birthdate, int companyId, boolean isActive) {
        String sql = "INSERT INTO employee (first_name, middle_name, last_name, phone, email, birthdate, company_id, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, firstName);
            statement.setString(2, middleName);
            statement.setString(3, lastName);
            statement.setString(4, phone);
            statement.setString(5, email);
            statement.setDate(6, birthdate);
            statement.setInt(7, companyId);
            statement.setBoolean(8, isActive);
            int rowsAffected = statement.executeUpdate();
            System.out.println("Added " + rowsAffected + " employee(s)");
        } catch (SQLException e) {
            System.err.println("Error adding employee: " + e.getMessage());
        }
    }

    // Method to view all employees
    @Step("Viewing all employees")
    public static void viewAllEmployees() {
        String sql = "SELECT * FROM employee";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                System.out.println(id + "-" + firstName);
            }
        } catch (SQLException e) {
            System.err.println("Error viewing all employees: " + e.getMessage());
        }
    }

    // Method to view employee information by ID
    @Step("Viewing employee by ID: {employeeId}")
    public static void viewEmployeeById(int employeeId) {
        String sql = "SELECT * FROM employee WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String firstName = resultSet.getString("first_name");
                    int companyId = resultSet.getInt("company_id");
                    System.out.println(id + "-" + firstName + companyId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing employee by id: " + e.getMessage());
        }
    }

    // Method to delete employee by ID
    @Step("Deleting employee by ID: {employeeId}")
    public static void deleteEmployeeById(int employeeId) {
        String sql = "DELETE FROM employee WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            int rowsAffected = statement.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " employee(s)");
        } catch (SQLException e) {
            System.err.println("Error deleting employee by id: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            // Adding a new employee
            addEmployee("Arthur", "Doylee", "Conan", "345-789", "arthurio@gmail.com", Date.valueOf("1859-09-12"), 11039, true);
            // Viewing the list of employees
            viewAllEmployees();
            // Viewing employee by ID
            int employeeId = 1; // You can specify the employee ID for viewing
            viewEmployeeById(employeeId);
            // Deleting employee by ID
            deleteEmployeeById(employeeId);
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
