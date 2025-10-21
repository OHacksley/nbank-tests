package Iteration_1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class CreateUserTest {

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    public static Stream<Arguments> UsersValidData() {
        return Stream.of(Arguments.of("artem123", "Artem2000%", "USER", "%s", "%s"),
                Arguments.of("Artem1-.", "Artem2000%", "USER", "%s", "%s"));
    }

    @MethodSource("UsersValidData")
    @ParameterizedTest
    public void adminCanGenerateUserWithCorrectData(String username, String password, String role, String errorKey, String errorValue) {
        String requestBody = String.format("""
                {
                "username": "%s",
                "password": "%s",
                "role": "%s"
                }
                """, username, password, role);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("%s"))
                .body("password", Matchers.not(Matchers.equalTo("Artem2000%")))
                .body("role", Matchers.equalTo("USER"));

    }

    //@CsvSource({
//username field validation
//            "  , Password33$, USER, qqq"
//    })
    public static Stream<Arguments> userInvalidData() {
        //Username field validation
        return Stream.of(Arguments.of("  ", "Password33$", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab", "Password33$", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("abc$", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("abc%", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"));
    }


    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidData(String username, String password, String role, String errorKey, String errorValue) {
        String requestBody = String.format("""
                {
                "username": "%s",
                "password": "%s",
                "role": "%s"
                }
                """, username, password, role);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("username", Matchers.equalTo("artem23"))
                .body("password", Matchers.not(Matchers.equalTo("Artem2000%")))
                .body(errorKey, Matchers.equalTo(errorValue));

    }
}

