package tests;

import io.qameta.allure.*;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import model.Company;
import org.junit.jupiter.api.*;
import utils.ConfigLoader;

import java.sql.*;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@Epic("Company Tests")
@Feature("Company Management")
public class CompanyTests extends AuthBase {

    private static final Company newCompany1 = new Company("TestCompany", "TestDescription");
    private static final Company newCompany2 = new Company("Sherlock Holmes", "Private detective agency");

    @Test
    @Story("Create Company")
    @DisplayName("Positive: Verify that creating a company is available")
    @Description("Test to verify that creating a company via API is available")
    @Severity(SeverityLevel.CRITICAL)
    public void createNewCompany() {
        int idCompany = createNewCompanyApi(newCompany2);

        given()
                .log().all()
                .queryParam("id", idCompany)
                .get(ConfigLoader.getUrlCompany() + "/" + idCompany)
                .then()
                .log().all()
                .statusCode(200)
                .body("name", equalTo(newCompany2.getName()));

        deleteCompany(idCompany);
    }

    @Test
    @Story("Verify Company Active")
    @DisplayName("Positive: Verify that company is active after creation")
    @Description("Test to verify that a company is active after being created")
    @Severity(SeverityLevel.NORMAL)
    public void verifyCompanyIsActive() {
        int idCompany = createNewCompanyApi(newCompany2);

        given()
                .log().all()
                .queryParam("id", idCompany)
                .get(ConfigLoader.getUrlCompany() + "/" + idCompany)
                .then()
                .log().all()
                .defaultParser(Parser.JSON)
                .statusCode(200)
                .body("isActive", equalTo(true));

        deleteCompany(idCompany);
    }

    @Test
    @Story("Create Company without Auth")
    @DisplayName("Negative: Verify creating a company without authentication fails")
    @Description("Test to verify that creating a company without authentication fails")
    @Severity(SeverityLevel.BLOCKER)
    public void createCompanyWithoutAuth() {
        given()
                .body(newCompany2.getJsonString())
                .contentType(ContentType.JSON)
                .when().post(ConfigLoader.getUrlCompany())
                .then()
                .defaultParser(Parser.JSON)
                .statusCode(401);
    }

    @Test
    @Story("Get Company By ID")
    @DisplayName("Positive: Get Company By ID")
    @Description("Test to retrieve a company by its ID")
    @Severity(SeverityLevel.NORMAL)
    public void getCompanyById() {
        int companyId = createNewCompanyApi(newCompany1);

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

        deleteCompany(companyId);
    }

    @Test
    @Story("Delete Company")
    @DisplayName("Ensure deletedAt field is set when company is deleted")
    @Description("Test to ensure that the deletedAt field is set when a company is deleted")
    @Severity(SeverityLevel.CRITICAL)
    public void deletedCompanyHasDeletedAtSet() throws SQLException {
        int companyId = createNewCompanyApi(newCompany1);

        deleteCompany(companyId);

        LocalDateTime deletedAt = getDeletedAtFromDatabase(companyId);
        Assertions.assertNotNull(deletedAt, "deletedAt field is not set for the deleted company");

        System.out.println("deletedAt field is set to: " + deletedAt);
    }

    private int createNewCompanyApi(Company company) {
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

    private void deleteCompany(int idCompany) {
        Response response = given()
                .log().all()
                .header("x-client-token", TOKEN)
                .get(ConfigLoader.getUrlCompany() + "/delete/" + idCompany)
                .then()
                .log().all()
                .defaultParser(Parser.JSON)
                .statusCode(200)
                .extract().response();

        String responseBody = response.getBody().asString();
        if (responseBody != null && !responseBody.trim().isEmpty()) {
            response.then().body("id", equalTo(idCompany));
        } else {
            System.out.println("Deleted company with ID: " + idCompany);
        }
    }

    private LocalDateTime getDeletedAtFromDatabase(int companyId) throws SQLException {
        String query = "SELECT deleted_at FROM company WHERE id = ?";
        LocalDateTime deletedAt = null;

        try (Connection connection = DriverManager.getConnection(ConfigLoader.getConnectionString(), ConfigLoader.getUserDB(), ConfigLoader.getPasswordDB());
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
