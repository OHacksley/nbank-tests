package api.requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public abstract class PutRequest {

    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public PutRequest(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }

    public abstract ValidatableResponse put(String newName);
}
