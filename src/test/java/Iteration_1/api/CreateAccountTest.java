package Iteration_1.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.DataBaseSteps;
import common.extensions.ApiVersionExtension;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.steps.AdminAPISteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ApiVersionExtension.class)
public class CreateAccountTest extends BaseTest {


    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminAPISteps.createUser();

        CreateAccountResponse createAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>
                (Endpoint.ACCOUNTS, RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.entityWasCreated(),
                        "userCanCreateAccountTest")
                .post(null);

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());

        DaoAndModelAssertions.assertThat(createAccountResponse, accountDao).match();
    }
}



