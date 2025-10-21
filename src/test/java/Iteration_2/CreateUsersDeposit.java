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
import static org.hamcrest.Matchers.*;

public class CreateUsersDeposit {

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void UsersDepositCorrectSum() {

        //создание пользователя
        /*given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                        "username": "Depo2",
                        "password": "Artem2000%",
                        "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        //Получаем токен юзера
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                        "username": "Depo2",
                        "password": "Artem2000%"
                        }""")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //Создаем аккаунт(счет)
                Integer usersId = given()
                .header("Authorization", "RGVwbzI6QXJ0ZW0yMDAwJQ==")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                        .extract()
                        .path("id");

        //Вносим депозит с корректной суммой
        given()
                .header("Authorization", "RGVwbzI6QXJ0ZW0yMDAwJQ==")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                        "id": "%d",
                        "balance": 2500.0
                        }
                        """, usersId))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }*/

        given()
                //Basic VHJhbnNmZXIyOkFydGVtMjAwMCU=     id = 5
                //Basic RGVwbzI6QXJ0ZW0yMDAwJQ==          id =3
                .header("Authorization", "Basic VHJhbnNmZXIyOkFydGVtMjAwMCU=")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                        "id": 5,
                        "balance": 2500.0
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void chekAccountsTransfers() {
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                        "username": "Depo2",
                        "password": "Artem2000%"
                        }""")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        given()
                .header("Authorization", "RGVwbzI6QXJ0ZW0yMDAwJQ==")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                        "id": 3,
                        "balance": 2500.0
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        Integer balance = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .get("http://localhost:4111/api/v1/accounts/3/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().path("balance");
    }

    public static Stream<Arguments> depositInvalidValues() {
        return Stream.of(Arguments.of(3, -0.1),
                Arguments.of(3, 0.0),
                Arguments.of(3, 5000.1),
                Arguments.of(3, 5000.0));
    }

    @MethodSource("depositInvalidValues")
    @ParameterizedTest
    public void depositWithInvalidData(int id, double balance) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic RGVwbzI6QXJ0ZW0yMDAwJQ==")
                .body(String.format("""
                        {
                        "id": %d,
                        "balance": %f
                        }
                        """, id, balance))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void depositToForeignAcc() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic RGVwbzI6QXJ0ZW0yMDAwJQ==")
                .body("""
                        {
                        "id": 2,
                        "balance": 1000
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(allOf(
                        greaterThanOrEqualTo(400),
                        not(equalTo(401))));
    }

    @Test
    public void depositToIncorrectAcc() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic RGVwbzI6QXJ0ZW0yMDAwJQ==")
                .body("""
                        {
                        "id": 0,
                        "balance": 1000
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(allOf(
                        greaterThanOrEqualTo(400),
                        not(equalTo(401))));
    }
}


