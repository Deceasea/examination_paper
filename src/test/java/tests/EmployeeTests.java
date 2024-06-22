package tests;

import com.github.javafaker.Faker;
import db.EmployeeRep;
import db.EmployeeRepJDBC;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import model.Company;
import model.Employee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ConfigLoader;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Employee Management")
@Feature("Employee Tests")
public class EmployeeTests extends AuthBase {

    public static final Faker faker = new Faker();
    public static final EmployeeRep repositoryJDBC = new EmployeeRepJDBC();
    private int employeeIDToDelete;

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up: delete the employee created during the test
        repositoryJDBC.deleteEmployeeByIdDB(employeeIDToDelete);
    }

    @Test
    @DisplayName("Positive: Add employees and get list")
    @Story("Add Employees")
    @Description("Test to verify adding multiple employees and retrieving their list via API")
    @Severity(SeverityLevel.CRITICAL)
    public void createAndListEmployees() throws SQLException {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi();
        Employee[] employeesAPI = {
                createRandomEmployee(idCompany),
                createRandomEmployee(idCompany),
                createRandomEmployee(idCompany)
        };
        int[] employeeIds = new int[employeesAPI.length];
        for (int i = 0; i < employeesAPI.length; i++) {
            // Create each employee and get their IDs
            employeeIds[i] = createNewEmployeeApi(employeesAPI[i], idCompany);
        }

        // Prepare expected and actual lists of employee IDs
        List<Integer> expectedIds = Arrays.stream(employeeIds).boxed().toList();

        List<Integer> actualIds = given()
                .get(ConfigLoader.getUrlEmployee() + "?company=" + idCompany)
                .then().log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().getList("id");

        // Assert that all expected IDs are present in the actual list
        assertTrue(actualIds.containsAll(expectedIds), "Actual IDs: " + actualIds + " do not contain all expected IDs: " + expectedIds);

        // Clean up: delete the created company and employees
        deleteCompany(idCompany);
        for (int id : employeeIds) {
            employeeIDToDelete = id;
        }
    }

    @Test
    @DisplayName("Positive: Add New Employee")
    @Story("Add Employee")
    @Description("Test to verify adding a new employee and retrieving their details from the database")
    @Severity(SeverityLevel.CRITICAL)
    public void createNewEmployee() throws SQLException {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi();
        Employee employeeAPI = createRandomEmployee(idCompany);

        // Create a new employee and get their ID
        int idEmployee = createNewEmployeeApi(employeeAPI, idCompany);

        // Retrieve the employee from the database and verify their details
        Employee employeeDb = repositoryJDBC.getEmployeeByIdDB(idEmployee);
        assertEquals(idEmployee, employeeDb.getId());
        assertEquals(employeeAPI.getFirstName(), employeeDb.getFirstName());
        assertTrue(employeeDb.isActive());

        // Clean up: delete the created company and employee
        deleteCompany(idCompany);
        employeeIDToDelete = employeeDb.getId();
    }
    @Test
    @DisplayName("Positive: Ensure inactive employee does not appear in list")
    @Story("Inactive Employee Filtering")
    @Description("Test to verify that inactive employees are filtered out from the employee list")
    @Severity(SeverityLevel.NORMAL)
    public void inactiveEmployeeNotInList() throws SQLException {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi();

        // Create active and inactive employees
        Employee activeEmployee = createRandomEmployee(idCompany);
        Employee inactiveEmployee = createRandomEmployee(idCompany);
        inactiveEmployee.setActive(false); // Set the employee as inactive

        // Create both employees via API and get their IDs
        int activeEmployeeId = createNewEmployeeApi(activeEmployee, idCompany);
        int inactiveEmployeeId = createNewEmployeeApi(inactiveEmployee, idCompany);

        // Log employee IDs for debugging
        System.out.println("Active Employee ID: " + activeEmployeeId);
        System.out.println("Inactive Employee ID: " + inactiveEmployeeId);

        // Retrieve the list of active employees for the company
        List<Integer> actualIds = given()
                .get(ConfigLoader.getUrlEmployee() + "?company=" + idCompany + "&isActive=true")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().getList("id");

        // Log actual IDs for debugging
        System.out.println("Actual Employee IDs: " + actualIds);

        // Verify that the active employee ID is in the list
        assertTrue(actualIds.contains(activeEmployeeId), "Active employee ID is not in the list");

        // Verify that the inactive employee ID is not in the list
        assertFalse(actualIds.contains(inactiveEmployeeId), "Inactive employee ID should not be in the list");

        // Clean up: delete the created company and employees
        deleteCompany(idCompany);
        employeeIDToDelete = activeEmployeeId;
        repositoryJDBC.deleteEmployeeByIdDB(inactiveEmployeeId);
    }



    @Test
    @DisplayName("Positive: Add a new employee to the company")
    @Story("Add Employee")
    @Description("Test to verify adding a new employee to the company via API")
    @Severity(SeverityLevel.NORMAL)
    public void addNewEmployeeToCompany() {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi();
        Employee employeeAPI = createRandomEmployee(idCompany);

        // Create a new employee and get their ID
        int idEmployee = createNewEmployeeApi(employeeAPI, idCompany);

        // Retrieve the employee by ID and verify their existence
        given()
                .log().all()
                .queryParam("id", idEmployee)
                .get(ConfigLoader.getUrlEmployee() + "/" + idEmployee)
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(idEmployee));

        // Clean up: delete the created company and employee
        deleteCompany(idCompany);
    }

    @Test
    @DisplayName("Positive: Get the list of employees")
    @Story("Get Employees List")
    @Description("Test to verify retrieving the list of employees for a company via API")
    @Severity(SeverityLevel.NORMAL)
    public void getListEmployee() {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi();
        Employee employeeAPI = createRandomEmployee(idCompany);

        // Create a new employee and get their ID
        int idEmployee = createNewEmployeeApi(employeeAPI, idCompany);

        // Retrieve the list of employees for the company
        given()
                .get(ConfigLoader.getUrlEmployee() + "?company=" + idCompany)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().getList("$");

        // Clean up: delete the created company and employee
        deleteCompany(idCompany);
    }

    @Test
    @DisplayName("Positive: Get Employee Info by ID")
    @Story("Get Employee Info")
    @Description("Test to verify retrieving employee information by ID from the database")
    @Severity(SeverityLevel.NORMAL)
    public void getEmployeeById() throws SQLException {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi();
        Employee employeeDb = createRandomEmployee(idCompany);

        // Create a new employee in the database and get their ID
        int newEmployeeId_DB = repositoryJDBC.createEmployeeDB(employeeDb);

        // Retrieve the employee by their ID and verify their details
        given()
                .get(ConfigLoader.getUrlEmployee() + "/" + newEmployeeId_DB)
                .then()
                .statusCode(200)
                .body("firstName", equalTo(employeeDb.getFirstName()))
                .body("id", equalTo(newEmployeeId_DB));

        // Clean up: delete the created company and employee
        deleteCompany(idCompany);
        employeeIDToDelete = newEmployeeId_DB;
    }

    @Test
    @DisplayName("Positive: Update employee information by id")
    @Story("Update Employee Info")
    @Description("Test to verify updating employee information by ID via API")
    @Severity(SeverityLevel.NORMAL)
    public void updateEmployeeById() {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi();
        Employee employeeAPI = createRandomEmployee(idCompany);

        // Create a new employee and get their ID
        int idEmployee = createNewEmployeeApi(employeeAPI, idCompany);

        // Prepare JSON for updating employee details
        String updatingEmployee = """
                {
                    "firstName": "John",
                    "middleName": "Doe",
                    "lastName": "Smith",
                    "companyId": %d,
                    "email": "john.doe@gmail.com",
                    "phone": "123-456-7890",
                    "birthdate": "1992-03-05",
                    "isActive": true
                }
                """.formatted(idCompany);

        System.out.println("Updating employee JSON: " + updatingEmployee); // Adding logging

        // Update employee details via API and verify the updated email
        given()
                .header("x-client-token", TOKEN)
                .body(updatingEmployee)
                .contentType(ContentType.JSON)
                .when().patch(ConfigLoader.getUrlEmployee() + "/" + idEmployee)
                .then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("email", equalTo("john.doe@gmail.com"));

        // Clean up: delete the created company and employee
        deleteCompany(idCompany);
    }

    @Test
    @DisplayName("Negative: Add Employee Without Authentication")
    @Story("Negative Cases")
    @Description("Test to verify adding an employee without authentication results in unauthorized status")
    @Severity(SeverityLevel.NORMAL)
    public void addEmployeeWithoutAuth() {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi();
        Employee employeeAPI = createRandomEmployee(idCompany);

        // Attempt to add an employee without authentication and verify unauthorized status
        given()
                .log().all()
                .body(employeeAPI.getJsonString(idCompany))
                .contentType(ContentType.JSON)
                .when().post(ConfigLoader.getUrlEmployee())
                .then().log().all()
                .defaultParser(Parser.JSON)
                .statusCode(401);

        // Clean up: delete the created company
        deleteCompany(idCompany);
    }

    @Test
    @DisplayName("Negative: Update Employee Info Without Authentication")
    @Story("Negative Cases")
    @Description("Test to verify updating employee information without authentication results in unauthorized status")
    @Severity(SeverityLevel.NORMAL)
    public void updateEmployeeWithoutAuth() throws SQLException {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi();
        Employee employeeAPI = createRandomEmployee(idCompany);

        // Create a new employee and get their ID
        int idEmployee = createNewEmployeeApi(employeeAPI, idCompany);

        // Prepare JSON for updating employee details
        String updatingEmployee = "{\"lastName\": \"Diego\"," +
                "\"email\": \"diego@gmail.ru\"," +
                " \"url\": \"text\"," +
                " \"phone\": \"789-3456\"," +
                "  \"isActive\": true }";

        // Attempt to update employee info without authentication and verify unauthorized status
        given()
                .body(updatingEmployee)
                .contentType(ContentType.JSON)
                .when().patch(ConfigLoader.getUrlEmployee() + "/" + idEmployee)
                .then()
                .defaultParser(Parser.JSON)
                .statusCode(401);

        // Clean up: delete the created company and employee
        deleteCompany(idCompany);
    }

    @Test
    @DisplayName("Negative: Add Employee Without Company")
    @Story("Negative Cases")
    @Description("Test to verify adding an employee without specifying company results in internal server error")
    @Severity(SeverityLevel.NORMAL)
    public void createNewEmployeeWithoutCompany() {
        // Prepare JSON for adding employee without specifying company
        String newEmployee = "{\"firstName\": \"Henry\"," +
                " \"middleName\": \"Heralt\"," +
                " \"lastName\": \"Cavill\"," +
                " \"companyId\": " + null + "," +
                "\"email\": \"vedmak@gmail.com\"," +
                " \"phone\": \"6789-56789\"," +
                " \"birthdate\": \"1289-03-08T20:50:57.525Z\"," +
                "  \"isActive\": true }";

        // Attempt to add an employee without specifying company and verify internal server error
        given()
                .log().all()
                .header("x-client-token", TOKEN)
                .body(newEmployee)
                .contentType(ContentType.JSON)
                .when().post(ConfigLoader.getUrlEmployee())
                .then().log().all()
                .defaultParser(Parser.JSON)
                .statusCode(500);
    }

    @Test
    @DisplayName("Negative: Access employee resource without authorization")
    @Story("Negative Cases")
    @Description("Test to verify accessing employee resource without authorization results in unauthorized status")
    @Severity(SeverityLevel.NORMAL)
    public void addEmployeeWithoutAuthorization() {
        // Create a new company and get its ID
        int idCompany = createNewCompanyApi();
        Employee employeeAPI = createRandomEmployee(idCompany);

        // Attempt to add an employee without authorization and verify unauthorized status
        given()
                .log().all()
                .body(employeeAPI.getJsonString(idCompany))
                .contentType(ContentType.JSON)
                .when().post(ConfigLoader.getUrlEmployee())
                .then().log().all()
                .defaultParser(Parser.JSON)
                .statusCode(401);

        // Clean up: delete the created company
        deleteCompany(idCompany);
    }

    private int createNewCompanyApi() {
        // Create a new company via API and return its ID
        Company newCompany = new Company();
        return given()
                .header("x-client-token", TOKEN)
                .body(newCompany.getJsonString())
                .contentType(ContentType.JSON)
                .when().post(ConfigLoader.getUrlCompany())
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");
    }

    private int createNewEmployeeApi(Employee employee, int idCompany) {
        // Create a new employee via API and return their ID
        Response response = given()
                .log().all()
                .header("x-client-token", TOKEN)
                .body(employee.getJsonString(idCompany))
                .contentType(ContentType.JSON)
                .when().post(ConfigLoader.getUrlEmployee())
                .then().log().all()
                .statusCode(201)
                .defaultParser(Parser.JSON)
                .extract().response();
        return response.jsonPath().getInt("id");
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


    private Employee createRandomEmployee(int idCompany) {
        // Create a random employee for testing purposes
        LocalDate birthdate = LocalDate.of(
                faker.random().nextInt(1950, 2000),
                faker.random().nextInt(1, 12),
                faker.random().nextInt(1, 28)
        );
        return new Employee(
                faker.name().firstName(),
                faker.name().username(),
                faker.name().lastName(),
                idCompany,
                faker.internet().emailAddress(),
                faker.phoneNumber().cellPhone(),
                birthdate.toString(),
                true
        );
    }
}