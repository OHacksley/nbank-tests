package Iteration_2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ChangeNameProfile {

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void UpdateCustomerProfile() {
        String name = "Artem Artemovov";
        String authHeader = "Basic VHJhbnNmZXIyOkFydGVtMjAwMCU=";

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", authHeader)
                .body(String.format("""
                        {
                        "name": "%s"
                        }
                        """, name))
                .when()
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Profile updated successfully"))
                .body("customer.name", equalTo(name));

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", authHeader)
                .when()
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(name));
    }

    public static Stream<Arguments> invalidNamesValues() {
        return Stream.of(Arguments.of("Artem_art"),
                Arguments.of("Artem artem artem"),
                Arguments.of("Artem-artem"));
    }
    @MethodSource("invalidNamesValues")
    @ParameterizedTest
    public void UpdateCustomerProfileNameAtOneWord(String name) {

        String authHeader = "Basic VHJhbnNmZXIyOkFydGVtMjAwMCU=";

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", authHeader)
                .body(String.format("""
                        {
                        "name": "%s"
                        }
                        """, name))
                .when()
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Profile updated successfully"))
                .body("customer.name", equalTo(name));

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", authHeader)
                .when()
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(name));
    }
}
