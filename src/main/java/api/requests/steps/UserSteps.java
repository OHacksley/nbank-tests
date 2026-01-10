package api.requests.steps;

import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<CreateAccountResponse> getAllAccounts() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOK()).getAll(CreateAccountResponse[].class);

    }

    public CustomerProfileResponse getProfileInfo () {
        return new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOK())
                .getWithoutId();
    }

    public DepositResponse makeDeposit (Long accountId) {
        DepositRequest depositRequest = DepositRequest.builder()
                .id(accountId)
                .balance(DepositAmount.STANDARD.getValue())
                .build();
        return new ValidatedCrudRequester<DepositResponse>(Endpoint.DEPOSIT,
                RequestSpecs.authAsUser(username,password),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);
    }
}
