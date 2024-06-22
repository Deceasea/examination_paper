package dbservice;

import io.qameta.allure.Step;
import utils.ConfigLoader;

import java.sql.*;

public class DBServiceCompany {

    // Method to add a new company
    @Step("Adding a new company")
    public static int addCompany(String name, String description) {
        String sql = "INSERT INTO company (name, description) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);
            statement.setString(2, description);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating company failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating company failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding company: " + e.getMessage());
            return -1; // Return -1 or handle error as appropriate
        }
    }

    // Method to view all companies
    @Step("Viewing all companies")
    public static void viewAllCompanies() {
        String sql = "SELECT * FROM company";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.println(id + "-" + name);
            }
        } catch (SQLException e) {
            System.err.println("Error viewing all companies: " + e.getMessage());
        }
    }

    // Method to view company information by ID
    @Step("Viewing company by ID: {companyId}")
    public static void viewCompanyById(int companyId) {
        String sql = "SELECT * FROM company WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, companyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    boolean isActive = resultSet.getBoolean("active");
                    System.out.println(id + "-" + name + "-" + description + "-" + isActive);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing company by id: " + e.getMessage());
        }
    }

    // Method to delete company by ID
    @Step("Deleting company by ID: {companyId}")
    public static void deleteCompanyById(int companyId) {
        String sql = "DELETE FROM company WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, companyId);
            int rowsAffected = statement.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " company(s)");
        } catch (SQLException e) {
            System.err.println("Error deleting company by id: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            // Adding a new company
            int companyId = addCompany("TestCompany", "123 Main St");
            System.out.println("Created company with ID: " + companyId);

            // Viewing the list of companies
            viewAllCompanies();

            // Viewing company by ID
            int companyIdToView = 1; // Specify the company ID to view
            viewCompanyById(companyIdToView);

            // Deleting company by ID
            deleteCompanyById(companyIdToView);
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
