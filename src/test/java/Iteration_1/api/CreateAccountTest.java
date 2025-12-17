package Iteration_1.api;

import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {


    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminSteps.createUser();

        new CrudRequester(Endpoint.ACCOUNTS, RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null);
    }
}



