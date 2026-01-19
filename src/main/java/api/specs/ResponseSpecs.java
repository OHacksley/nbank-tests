package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class ResponseSpecs {
    private ResponseSpecs() {}

    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest(String errorKey, List<String> errorValues) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(errorKey, Matchers.containsInAnyOrder(errorValues.toArray()))
                .build();
    }
    public static ResponseSpecification requestReturnsBadRequestWithText(String expectedMessage) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(equalTo(expectedMessage))
                .build();
    }

    public static ResponseSpecification requestReturnsForbiddenWithText(String expectedMessage) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .expectBody(equalTo(expectedMessage))
                .build();
    }
    public static ResponseSpecification requestReturnsBadRequestWithContentText(String expectedText) {
        return defaultResponseBuilder()
                .expectStatusCode(400)
                .expectContentType(ContentType.JSON) // или ContentType.ANY
                .expectBody(Matchers.equalTo(expectedText))
                .build();
    }

}
