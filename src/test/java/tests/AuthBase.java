package tests;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import utils.ConfigLoader;

import static io.restassured.RestAssured.given;

public class AuthBase {

    public static String TOKEN;

    @BeforeAll
    @DisplayName("Authenticate User")
    public static void authenticateUser() {
        String creds = """
                {
                  "username": "tecna",
                  "password": "tecna-fairy"
                }
                """;

        // Perform authentication request
        TOKEN = given()
                .log().all()
                .body(creds)
                .contentType(ContentType.JSON)
                .when().post(ConfigLoader.getUrlAuth())
                .then().log().all()
                .statusCode(201)
                .extract().path("userToken");
    }
}
