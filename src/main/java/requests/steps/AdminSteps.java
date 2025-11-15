package requests.steps;

import generators.RandomModelGenerator;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.CreateUserResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.ArrayList;
import java.util.List;

public class AdminSteps {
    private static final List<Long> createdUserId = new ArrayList<>();

    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse user = new ValidatedCrudRequester<CreateUserResponse>(
                Endpoint.ADMIN_USER,
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        createdUserId.add(user.getId());

        return userRequest;
    }

    //Метод для создания счета пользователя

    public static CreateAccountResponse createUserAccount(CreateUserRequest userRequest) {

        CreateAccountResponse accountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                Endpoint.ACCOUNTS,
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        return  accountResponse;
    }
}

