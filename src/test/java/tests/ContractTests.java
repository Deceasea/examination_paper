package tests;
import io.restassured.http.ContentType;
import model.Company;
import model.Employee;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsEqual.equalTo;

public class ContractTests {

    // URL endpoints
    public static final String URL_COMPANY = "https://x-clients-be.onrender.com/company";
    public static final String URL_EMPLOYEE = "https://x-clients-be.onrender.com/employee";
    public static final String URL_AUTH = "https://x-clients-be.onrender.com/auth/login";

    // Авторизационный токен
    public static String TOKEN;

    // Создание новой компании и нового сотрудника для тестов
    public static Company newCompany = new Company();
    public static Employee newEmployee = new Employee();

    // Метод для авторизации перед выполнением всех тестов
    @BeforeAll
    @DisplayName("Authorization")
    public static void getToken() {
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

    // Тест для проверки доступности создания компании
    @Test
    @DisplayName("Positive: Verify that creating a company is available")
    public void createNewCompany() {
        int idCompany = given()
                .header("x-client-token", TOKEN)
                .body(newCompany.getJsonString())
                .contentType(ContentType.JSON)
                .when().post(URL_COMPANY)
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");
        // Проверяем, что компания создалась
        given()
                .log().all()
                .queryParam("id", idCompany)
                .get(URL_COMPANY + "/" + idCompany)
                .then()
                .log().all()
                .statusCode(200)
                .body("name", equalTo("Sherlock Holmes"));
        // Удаление созданной компании после теста
        deleteCompany(idCompany);
    }

    // Тест для добавления нового сотрудника в компанию
    @Test
    @DisplayName("Positive: Add a new employee to the company")
    public void createNewEmployee() {
        int idCompany = given()
                .header("x-client-token", TOKEN)
                .body(newCompany.getJsonString())
                .contentType(ContentType.JSON)
                .when().post(URL_COMPANY)
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");

        int idEmployee = given()
                .log().all()
                .header("x-client-token", TOKEN)
                .body(newEmployee.getJsonString(idCompany))
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");
        // Проверяем, что сотрудник создался
        given()
                .log().all()
                .queryParam("id", idEmployee)
                .get(URL_EMPLOYEE + "/" + idEmployee)
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(idEmployee));

        // Удаление созданной компании после теста
        deleteCompany(idCompany);
    }

    // Тест для получения списка сотрудников
    @Test
    @DisplayName("Positive: Get the list of employees")
    public void getListEmployee() {
        int idCompany = given()
                .header("x-client-token", TOKEN)
                .body(newCompany.getJsonString())
                .contentType(ContentType.JSON)
                .when().post(URL_COMPANY)
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");

        int idEmployee = given().log().all()
                .header("x-client-token", TOKEN)
                .body(newEmployee.getJsonString(idCompany))
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");

        // Получаем список сотрудников
        List<Map<String, Object>> employeesList = given()
                .get(URL_EMPLOYEE + "?company=" + idCompany)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().getList("$");

        // Выводим список сотрудников в консоль
        System.out.println("List of employees:");
        employeesList.forEach(employee -> System.out.println(employee));

        // Удаление созданной компании после теста
        deleteCompany(idCompany);
    }


    // Тест для получения информации о сотруднике по его id
    @Test
    @DisplayName("Positive: Get an employee by id")
    public void getEmployeeById() {
        int idCompany = given()
                .header("x-client-token", TOKEN)
                .body(newCompany.getJsonString())
                .contentType(ContentType.JSON)
                .when().post(URL_COMPANY)
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");

        int idEmployee = given()
                .log().all()
                .header("x-client-token", TOKEN)
                .body(newEmployee.getJsonString(idCompany))
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");

        given()
                .get(URL_EMPLOYEE + "/" + idEmployee)
                .then().log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(idEmployee));

        // Удаление созданной компании после теста
        deleteCompany(idCompany);
    }

    // Тест для изменения информации о сотруднике по его id
    @Test
    @DisplayName("Positive: Update employee information by id")
    public void updateEmployeeById() {
        // Создаем нового сотрудника
        int idCompany = given()
                .header("x-client-token", TOKEN)
                .body(newCompany.getJsonString())
                .contentType(ContentType.JSON)
                .when().post(URL_COMPANY)
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");

        int idEmployee = given()
                .header("x-client-token", TOKEN)
                .body(newEmployee.getJsonString(idCompany))
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");

        // Обновляем информацию о сотруднике
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

        given()
                .header("x-client-token", TOKEN)
                .body(updatingEmployee)
                .contentType(ContentType.JSON)
                .when().patch(URL_EMPLOYEE + "/" + idEmployee)
                .then()
                .log().all()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("email", equalTo("john.doe@gmail.com"));

        // Удаление созданной компании после теста
        deleteCompany(idCompany);
    }


    // Тест для попытки доступа к ресурсу без авторизации
    @Test
    @DisplayName("Negative: Access employee resource without authorization")
    public void addEmployeeWithoutAuth() {
        // Попытка добавить сотрудника без авторизации
        int idCompany = given()
                .header("x-client-token", TOKEN)
                .body(newCompany.getJsonString())
                .contentType(ContentType.JSON)
                .when().post(URL_COMPANY)
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");

        Employee employeeAPI = new Employee();
        given().log().all()
                .body(employeeAPI.getJsonString(idCompany))
                .contentType(ContentType.JSON)
                .when().post(URL_EMPLOYEE)
                .then().log().all()
                .statusCode(401);

        // Удаление созданной компании после теста
        deleteCompany(idCompany);
    }

    // Метод для удаления компании по ее id
    private void deleteCompany(int idCompany) {
        given().log().all()
                .header("x-client-token", TOKEN)
                .get(URL_COMPANY + "/" + idCompany)
                .then()
                .statusCode(200)
                .body("id", equalTo(idCompany)).log().all();
    }
}
