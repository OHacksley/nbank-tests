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

public class CreateUserTransfers {

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void TranserValidSumm() {

        given()
                .header("Authorization", "Basic RGVwbzI6QXJ0ZW0yMDAwJQ==")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                        "senderAccountId": 3,
                        "receiverAccountId": 4,
                        "amount": 250.00
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        //Проверяем акк id=3
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic RGVwbzI6QXJ0ZW0yMDAwJQ==")
                .get("http://localhost:4111/api/v1/accounts/3/transactions")
                .then()
                .assertThat()
                .log().body()
                .statusCode(200)
                .body(
                        "find { it.type == 'TRANSFER_OUT' }.amount", equalTo(250.00f),
                        "find { it.type == 'TRANSFER_OUT' }.relatedAccountId", equalTo(2),
                        "find { it.type == 'TRANSFER_OUT' }.type", equalTo("TRANSFER_OUT"));


    }

    public static Stream<Arguments> transferInvalidValues() {
        return Stream.of(Arguments.of(3, 2, -0.1),
                Arguments.of(3, 2, 0.0),
                Arguments.of(3, 2, 10000.0),
                Arguments.of(3, 2, 10000.1));
    }

    @MethodSource("transferInvalidValues")
    @ParameterizedTest
    public void depositWithInvalidData(int senderAccountId, int receiverAccountId, double amount) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic RGVwbzI6QXJ0ZW0yMDAwJQ==")
                .body(String.format("""
                        {
                        "senderAccountId": %d,
                        "receiverAccountId": %d,
                        "amount": %f
                        }
                        """, senderAccountId, receiverAccountId, amount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void balanceLessTransferSumm() {
        //создаем пользователя
        /*
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                        "username": "Transfer2",
                        "password": "Artem2000%",
                        "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                        "username": "Transfer2",
                        "password": "Artem2000%"
                        }""")
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        //Создаем аккаунт(счет)
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
    }

    //Создаем второй аккаунт(счет)
    //Basic VHJhbnNmZXIyOkFydGVtMjAwMCU=

        @Test
        public void createUser2in1Acc() {
            given()
                    .header("Authorization", "Basic VHJhbnNmZXIyOkFydGVtMjAwMCU=")
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .post("http://localhost:4111/api/v1/accounts")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatus.SC_CREATED);


     */
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VHJhbnNmZXIyOkFydGVtMjAwMCU=")
                .body("""
                        {
                        "senderAccountId": 5,
                        "receiverAccountId": 6,
                        "amount": 9900.00
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

    }


    @Test
    public void TransferToForeignAcc() {

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VHJhbnNmZXIyOkFydGVtMjAwMCU=")
                .body("""
                        {
                        "senderAccountId": 5,
                        "receiverAccountId": 6,
                        "amount": 100.00
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

    }
}




