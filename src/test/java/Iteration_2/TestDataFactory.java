package Iteration_2;

import generators.RandomData;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.TestAccData;
import models.UserRole;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class TestDataFactory extends TestAccData {

        public static TestAccData createTransferTestData() {

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

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated()).post(user1);
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated()).post(user2);

        CreateAccountResponse account1Response = new CreateAccountRequester(
                RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        CreateAccountResponse account2Response = new CreateAccountRequester(
                RequestSpecs.authAsUser(user2.getUsername(), user2.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        return new TestAccData(
                account1Response.getId(),
                account2Response.getId(),
                user1.getUsername(),
                user1.getPassword(),
                user2.getUsername(),
                user2.getPassword()
        );}
}
