package db;

import model.Company;
import utils.ConfigLoader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompanyRepJDBC implements CompanyRep {

    private static final String SQL_INSERT_COMPANY = "INSERT INTO company (name, address, active) VALUES (?, ?, ?) RETURNING id";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM company WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM company WHERE id = ?";
    private static final String SQL_UPDATE_DELETED_AT = "UPDATE company SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?";

    @Override
    public int createCompany(Company company) throws SQLException {
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_COMPANY)) {

            statement.setString(1, company.getName());
            statement.setString(2, company.getAddress());
            statement.setBoolean(3, company.isActive());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                throw new SQLException("Failed to get generated key.");
            }
        }
    }

    @Override
    public Company getCompanyById(int id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCompany(resultSet);
                } else {
                    throw new SQLException("Company with id " + id + " not found.");
                }
            }
        }
    }

    @Override
    public void deleteCompanyById(int id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public void setDeletedAtForCompany(int id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_DELETED_AT)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean isCompanyExists(int companyId) {
        return false;
    }

    private Company mapResultSetToCompany(ResultSet resultSet) throws SQLException {
        Company company = new Company();
        company.setId(resultSet.getInt("id"));
        company.setName(resultSet.getString("name"));
        company.setAddress(resultSet.getString("address"));
        company.setActive(resultSet.getBoolean("active"));
        company.setDeletedAt(resultSet.getTimestamp("deleted_at"));
        return company;
    }

    @Override
    public List<Company> getCompaniesByActive(boolean isActive) throws SQLException {
        List<Company> companies = new ArrayList<>();
        String sql = "SELECT * FROM company WHERE active = ?";
        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, isActive);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    companies.add(mapResultSetToCompany(resultSet));
                }
            }
        }
        return companies;
    }

}
