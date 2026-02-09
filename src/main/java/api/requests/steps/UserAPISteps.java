package api.requests.steps;

import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;

public class UserAPISteps {
    private String username;
    private String password;

    public UserAPISteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<CreateAccountResponse> getAllAccounts() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOK()).getAll(CreateAccountResponse[].class);

    }

    public CustomerProfileResponse getProfileInfo (String newUsername) {
        return await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(30))
                .pollInSameThread()
                .ignoreException(RuntimeException.class)
                .until(() -> {
                            CustomerProfileResponse response = new ValidatedCrudRequester<CustomerProfileResponse>(
                                    Endpoint.CUSTOMER_PROFILE,
                                    RequestSpecs.authAsUser(username, password),
                                    ResponseSpecs.requestReturnsOK())
                                    .getWithoutId();

                            return response.getName().contentEquals(newUsername) ? response : null;
                        },
                        response -> response != null);
    }

    public DepositResponse makeDeposit (Long accountId) {
        DepositRequest depositRequest = DepositRequest.builder()
                .accountId(accountId)
                .balance(DepositAmount.STANDARD.getValue())
                .build();
        return new ValidatedCrudRequester<DepositResponse>(Endpoint.ACCOUNT_DEPOSIT,
                RequestSpecs.authAsUser(username,password),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);
    }
}
