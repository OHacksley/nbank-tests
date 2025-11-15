package Iteration_2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import models.AccountInfo;
import models.AllUsersResponse;
import models.CreateUserResponse;
import models.UserInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

import static io.restassured.RestAssured.given;

public class GetAllUsers {


    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @Test
    public void getAllUsersAsList() {
        //создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .get("http://localhost:4111/api/v1/admin/users")
                .then().statusCode(200);

    }

    @Test
    public void GetAllUsersImproved() {


        AllUsersResponse allUsers = new ValidatedCrudRequester<AllUsersResponse>(Endpoint.GET_ALL_USERS,
                RequestSpecs.adminSpec(), ResponseSpecs.requestReturnsOK())
                .getWithoutId();

    }
}