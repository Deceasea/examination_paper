package tests;

import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import model.Company;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ConfigLoader;

import java.sql.*;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class CompanyTests extends AuthBase {

    private static final Company newCompany1 = new Company("TestCompany", "TestDescription");
    private static final Company newCompany2 = new Company("Sherlock Holmes", "Private detective agency");

    @Test
    @DisplayName("Positive: Verify that creating a company is available")
    public void createNewCompany() {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi(newCompany2);

        // Verify company creation by retrieving it and checking its name
        given()
                .log().all()
                .queryParam("id", idCompany)
                .get(ConfigLoader.getUrlCompany() + "/" + idCompany)
                .then()
                .log().all()
                .statusCode(200)
                .body("name", equalTo(newCompany2.getName()));

        // Clean up: delete the created company
        deleteCompany(idCompany);
    }

    @Test
    @DisplayName("Positive: Verify that company is active after creation")
    public void verifyCompanyIsActive() {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi(newCompany2);

        // Verify that the created company is active
        given()
                .log().all()
                .queryParam("id", idCompany)
                .get(ConfigLoader.getUrlCompany() + "/" + idCompany)
                .then()
                .log().all()
                .defaultParser(Parser.JSON)
                .statusCode(200)
                .body("isActive", equalTo(true));

        // Clean up: delete the created company
        deleteCompany(idCompany);
    }

    @Test
    @DisplayName("Negative: Verify creating a company without authentication fails")
    public void createCompanyWithoutAuth() {
        // Attempt to create a company without authentication
        given()
                .body(newCompany2.getJsonString())
                .contentType(ContentType.JSON)
                .when().post(ConfigLoader.getUrlCompany())
                .then()
                .defaultParser(Parser.JSON)
                .statusCode(401);
    }

    @Test
    @DisplayName("Positive: Get Company By ID")
    public void getCompanyById() {
        // Create a new company and get its ID
        int companyId = createNewCompanyApi(newCompany1);

        // Retrieve the company by its ID and verify its existence
        String url = ConfigLoader.getUrlCompany() + "/" + companyId;
        System.out.println("Sending GET request to URL: " + url);

        given()
                .header("x-client-token", TOKEN)
                .header("Cache-Control", "no-cache")
                .get(url)
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(companyId));

        // Clean up: delete the created company
        deleteCompany(companyId);
    }

    @Test
    @DisplayName("Ensure deletedAt field is set when company is deleted")
    public void deletedCompanyHasDeletedAtSet() throws SQLException {
        // Create a new company and get its ID
        int companyId = createNewCompanyApi(newCompany1);

        // Delete the company via API
        deleteCompany(companyId);

        // Verify in the database that deletedAt is set for the company
        LocalDateTime deletedAt = getDeletedAtFromDatabase(companyId);
        Assertions.assertNotNull(deletedAt, "deletedAt field is not set for the deleted company");

        System.out.println("deletedAt field is set to: " + deletedAt);
    }

    /**
     * Helper method to create a new company via API and return its ID.
     *
     * @param company The Company object representing the new company.
     * @return The ID of the newly created company.
     */
    private int createNewCompanyApi(Company company) {
        // Perform API request to create a new company
        int idCompany = given()
                .header("x-client-token", TOKEN)
                .body(company.getJsonString())
                .contentType(ContentType.JSON)
                .when().post(ConfigLoader.getUrlCompany())
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");

        System.out.println("Created company ID: " + idCompany);
        return idCompany;
    }

    /**
     * Helper method to delete a company by ID.
     *
     * @param idCompany The ID of the company to delete.
     */
    private void deleteCompany(int idCompany) {
        // Perform API request to delete the company
        given()
                .log().all()
                .header("x-client-token", TOKEN)
                .get(ConfigLoader.getUrlCompany() + "/delete/" + idCompany)
                .then()
                .log().all()
                .defaultParser(Parser.JSON)
                .statusCode(200)
                .body("id", equalTo(idCompany))
                .log().all();

        System.out.println("Deleted company with ID: " + idCompany);
    }

    /**
     * Helper method to retrieve deletedAt field from the database for a company.
     *
     * @param companyId The ID of the company.
     * @return The value of deletedAt field from the database.
     * @throws SQLException If an SQL exception occurs.
     */
    private LocalDateTime getDeletedAtFromDatabase(int companyId) throws SQLException {
        String query = "SELECT deleted_at FROM company WHERE id = ?";
        LocalDateTime deletedAt = null;

        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(),ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, companyId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                deletedAt = resultSet.getTimestamp("deleted_at").toLocalDateTime();
            }
        }

        return deletedAt;
    }
}
