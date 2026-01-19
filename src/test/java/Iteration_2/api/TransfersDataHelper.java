package Iteration_2.api;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.DepositRequest;
import api.models.UserRole;
import api.requests.AdminCreateUserRequester;
import api.requests.CreateAccountRequester;
import api.requests.UserCreateDepositRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class TransfersDataHelper {
    static Long USER_1_ID;
    static Long USER_2_ID;
    static String USER_1_USERNAME;
    static String USER_2_USERNAME;
    static String USER_2_PASSWORD;
    static String USER_1_PASSWORD;

    static {
        CreateUserRequest user1 = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserRequest user2 = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(user1);

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(user2);

        CreateAccountResponse account1Response = new CreateAccountRequester(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        CreateAccountResponse account2Response = new CreateAccountRequester(RequestSpecs.authAsUser(user2.getUsername(), user2.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        USER_1_ID = account1Response.getId();
        USER_2_ID = account2Response.getId();
        USER_1_USERNAME = user1.getUsername();
        USER_1_PASSWORD = user1.getPassword();
        USER_2_USERNAME = user2.getUsername();
        USER_2_PASSWORD = user2.getPassword();

    }

    public static void depositToAccount (Long accountId, Double amount, String username, String password) {
        DepositRequest depositRequest = DepositRequest.builder()
                .accountId(accountId)
                .amount(amount)
                .build();

        new UserCreateDepositRequester(RequestSpecs.authAsUser(username, password), ResponseSpecs.requestReturnsOK())
                .post(depositRequest);
    }

}
