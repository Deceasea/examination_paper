package db;

import model.Company;

import java.sql.SQLException;
import java.util.List;

public interface CompanyRep {

    /**
     * Creates a company in the database.
     *
     * @param company The company to add.
     * @return The ID of the new company.
     * @throws SQLException If there is an error working with the database.
     */
    @io.qameta.allure.Step("Creating a company in the database")
    int createCompany(Company company) throws SQLException;

    /**
     * Retrieves a company from the database by its ID.
     *
     * @param id The ID of the company.
     * @return The company with the specified ID.
     * @throws SQLException If there is an error working with the database.
     */
    @io.qameta.allure.Step("Retrieving a company by ID from the database")
    Company getCompanyById(int id) throws SQLException;

    /**
     * Deletes a company from the database by its ID.
     *
     * @param id The ID of the company.
     * @throws SQLException If there is an error working with the database.
     */
    @io.qameta.allure.Step("Deleting a company by ID from the database")
    void deleteCompanyById(int id) throws SQLException;

    void setDeletedAtForCompany(int id) throws SQLException;

    boolean isCompanyExists(int companyId);

    List<Company> getCompaniesByActive(boolean isActive) throws SQLException;
}
