package tests;

import com.github.javafaker.Faker;
import db.EmployeeRep;
import db.EmployeeRepJDBC;
import io.restassured.http.ContentType;
import model.Company;
import model.Employee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BusinessTests {

    public static final Faker faker = new Faker();
    public static final String URL_COMPANY = "https://x-clients-be.onrender.com/company";
    public static final String URL_EMPLOYEE = "https://x-clients-be.onrender.com/employee";
    public static final String URL_AUTH = "https://x-clients-be.onrender.com/auth/login";
    public static final EmployeeRep repositoryJDBC = (EmployeeRep) new EmployeeRepJDBC();
    public static String TOKEN;
    public static Company newCompany = new Company();
    private int employeeIDToDelete;

    @BeforeAll
    @DisplayName("Positive: Auth")
    public static void getToken() {
        // Авторизация
        String creds = """
                {
                  "username": "tecna",
                  "password": "tecna-fairy"
                }
                """;
        TOKEN = given()
                .log().all()
                .body(creds)
                .contentType(ContentType.JSON)
                .when().post(URL_AUTH)
                .then().log().all()
                .statusCode(201)
                .extract().path("userToken");
    }

    @AfterEach
    public void tearDown() throws SQLException {
        repositoryJDBC.deleteEmployeeByIdDB(employeeIDToDelete);
    }

    @Test
    @DisplayName("Positive: Add employees and get list")
    public void createAndListEmployees() throws SQLException {
        // Создание новой компании
        int idCompany = createNewCompanyApi();
        // Создание нескольких сотрудников
        Employee[] employeesAPI = {
                createRandomEmployee(idCompany),
                createRandomEmployee(idCompany),
                createRandomEmployee(idCompany)
        };
        // Добавление сотрудников и получение их ID
        int[] employeeIds = new int[employeesAPI.length];
        for (int i = 0; i < employeesAPI.length; i++) {
            employeeIds[i] = createNewEmployeeApi(employeesAPI[i], idCompany);
        }

        // Ожидаемый список айдишников
        List<Integer> expectedIds = Arrays.stream(employeeIds).boxed().collect(Collectors.toList());

        // Получение списка фактических айдишников сотрудников
        List<Integer> actualIds = given()
                .get(URL_EMPLOYEE + "?company=" + idCompany)
                .then().log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().getList("id");

        // Проверка, что фактический список содержит все элементы из ожидаемого списка
        assertTrue(actualIds.containsAll(expectedIds));

        // Удаление компании и сохранение ID сотрудников для последующего удаления
        deleteCompany(idCompany);
        for (int id : employeeIds) {
            employeeIDToDelete = id;
        }
    }

    @Test
    @DisplayName("Positive: Add new employee")
    public void createNewEmployee() throws SQLException {
        // Создание новой компании и добавление сотрудника
        int idCompany = createNewCompanyApi();
        Employee employeeAPI = createRandomEmployee(idCompany);
        int idEmployee = createNewEmployeeApi(employeeAPI, idCompany);
        // Проверка добавленного сотрудника
        Employee employeeDb = repositoryJDBC.getEmployeeByIdDB(idEmployee);
        assertEquals(idEmployee, employeeDb.getId());
        assertEquals(employeeAPI.getFirstName(), employeeDb.getFirstName());
        assertTrue(employeeDb.isActive());
        // Удаление компании и сохранение ID сотрудника для последующего удаления
        deleteCompany(idCompany);
        employeeIDToDelete = employeeDb.getId();
    }

    @Test
    @DisplayName("Negative: Employee without auth")
    public void addEmployeeWithoutAuth() {
        // Попытка добавить сотрудника без авторизации
        int idCompany = createNewCompanyApi();
        Employee employeeAPI = createRandomEmployee(idCompany);
        given()
                .log().all()
                .body(employeeAPI.getJsonString(idCompany))
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(401);
        deleteCompany(idCompany);
    }

    @Test
    @DisplayName("Negative: Change info without auth")
    public void updateEmployeeWithoutAuth() throws SQLException {
        // Попытка изменить информацию о сотруднике без авторизации
        int idCompany = createNewCompanyApi();
        Employee employeeAPI = createRandomEmployee(idCompany);
        int idEmployee = createNewEmployeeApi(employeeAPI, idCompany);
        String updatingEmployee = "{\"lastName\": \"Diego\"," +
                "\"email\": \"diego@gmail.ru\"," +
                " \"url\": \"text\"," +
                " \"phone\": \"789-3456\"," +
                "  \"isActive\": true }";
        given()
                .body(updatingEmployee)
                .contentType(ContentType.JSON)
                .when().patch(URL_EMPLOYEE + "/" + idEmployee)
                .then()
                .statusCode(401);
        deleteCompany(idCompany);
    }

    @Test
    @DisplayName("Negative: Add employee without company")
    public void createNewEmployeeWithoutCompany() {
        // Попытка добавить сотрудника без указания компании
        String newEmployee = "{\"firstName\": \"Henry\"," +
                " \"middleName\": \"Heralt\"," +
                " \"lastName\": \"Cavill\"," +
                " \"companyId\": " + null + "," +
                "\"email\": \"vedmak@gmail.com\"," +
                " \"phone\": \"6789-56789\"," +
                " \"birthdate\": \"1289-03-08T20:50:57.525Z\"," +
                "  \"isActive\": true }";
        given()
                .log().all()
                .header("x-client-token", TOKEN)
                .body(newEmployee)
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(500);
    }

    @Test
    @DisplayName("Positive: Get info by ID")
    public void getEmployeeById() throws SQLException {
        // Получить информацию о сотруднике по ID
        int idCompany = createNewCompanyApi();
        Employee employeeDb = createRandomEmployee(idCompany);
        int newEmployeeId_DB = repositoryJDBC.createEmployeeDB(employeeDb);
        var employeeResponse = given()
                .get(URL_EMPLOYEE + "/" + newEmployeeId_DB)
                .then();
        assertEquals(employeeDb.getFirstName(), employeeResponse.extract().path("firstName"));
        assertEquals(newEmployeeId_DB, (int) employeeResponse.extract().path("id"));
        deleteCompany(idCompany);
        employeeIDToDelete = newEmployeeId_DB;
    }

    private void deleteCompany(int idCompany) {
        given()
                .log().all()
                .header("x-client-token", TOKEN)
                .get(URL_COMPANY + "/" + idCompany)
                .then()
                .statusCode(200)
                .body("id", equalTo(idCompany)).log().all();
    }

    private int createNewCompanyApi() {
        int idCompany = given()
                .header("x-client-token", TOKEN)
                .body(newCompany.getJsonString())
                .contentType(ContentType.JSON)
                .when().post(URL_COMPANY)
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");
        return idCompany;
    }

    private int createNewEmployeeApi(Employee employee, int idCompany) throws SQLException {
        // Создаем нового сотрудника
        given()
                .log().all()
                .header("x-client-token", TOKEN)
                .body(employee.getJsonString(idCompany))
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201);

        // Возвращаем ID сотрудника, который уже есть в объекте Employee
        return repositoryJDBC.createEmployee(employee);
    }

    // Создаем сотрудника с случайными данными
    private Employee createRandomEmployee(int idCompany) {
        // Генерируем случайную дату рождения
        LocalDate birthdate = LocalDate.of(
                faker.random().nextInt(1950, 2000), // Год
                faker.random().nextInt(1, 12), // Месяц
                faker.random().nextInt(1, 28) // День
        );

        // Создаем и возвращаем сотрудника с случайными данными
        return new Employee(
                faker.name().firstName(),
                faker.name().username(),
                faker.name().lastName(),
                idCompany,
                faker.internet().emailAddress(),
                faker.phoneNumber().cellPhone(),
                birthdate.toString(), // Преобразуем LocalDate в строку
                true // Значение "true" для поля "isActive"
        );
    }
}