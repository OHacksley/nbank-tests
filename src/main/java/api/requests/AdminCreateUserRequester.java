package api.requests;

import api.requests.skelethon.Endpoint;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.CreateUserRequest;

import static io.restassured.RestAssured.given;

public class AdminCreateUserRequester extends Request<CreateUserRequest> {
    public AdminCreateUserRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(CreateUserRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/admin/users")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
