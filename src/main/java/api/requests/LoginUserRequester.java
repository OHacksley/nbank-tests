package api.requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.LoginUserRequest;

import static io.restassured.RestAssured.given;

public class LoginUserRequester extends Request<LoginUserRequest>{

    public LoginUserRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(LoginUserRequest model) {
        return given()                                               //Жесткая связка : эндпоинт, JSON запроса и JSON ответа
                .spec(requestSpecification)                          //Параметр 1: спецификация запроса (хедеры)
                .body(model)                                         //Параметр 2: тело запроса
                .post("/api/v1/auth/login")                       //Параметр 3: эндпоинт(относит путь)
                .then()
                .assertThat()                                        //Параметр 4: спецификация ответа
                .spec(responseSpecification);
    }
}
