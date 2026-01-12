package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

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

    public static List<CreateUserResponse> getAllUsers() {
        return new ValidatedCrudRequester<CreateUserResponse>(
                Endpoint.ADMIN_USER,
                RequestSpecs.adminSpec(),
                        ResponseSpecs.requestReturnsOK()).getAll(CreateUserResponse[].class);

    }
}

