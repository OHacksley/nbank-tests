package api.requests.steps;

import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.helpers.StepLogger;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.assertj.core.api.Assertions;

import static io.restassured.RestAssured.given;

public class AccountSteps {
    private String username;
    private String password;

    public AccountSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public CreateAccountResponse createAccount() {
        return StepLogger.log("User " + username + " creates account", () -> {
            return new ValidatedCrudRequester<CreateAccountResponse>(
                    Endpoint.ACCOUNTS,
                    RequestSpecs.authAsUser(username, password),
                    ResponseSpecs.entityWasCreated()).post(null);
        });
    }

    public DepositResponse depositToAccount(Long accountId, Double amount) {
        return StepLogger.log("User " + username + " deposits " + amount + " to account " + accountId, () -> {
            DepositRequest depositRequest = DepositRequest.builder()
                    .accountId(accountId)
                    .amount(amount)
                    .build();

            return new ValidatedCrudRequester<DepositResponse>(
                    Endpoint.ACCOUNT_DEPOSIT,
                    RequestSpecs.authAsUser(username, password),
                    ResponseSpecs.requestReturnsOK()).post(depositRequest);
        });
    }

    public TransferResponse transferWithFraudCheck(Long senderAccountId, Long receiverAccountId, Double amount) {
        return StepLogger.log("User " + username + " transfers " + amount + " to " + receiverAccountId + " with fraud check", () -> {
            TransferRequest transferRequest = TransferRequest.builder()
                    .senderAccountId(senderAccountId)
                    .receiverAccountId(receiverAccountId)
                    .amount(amount)
                    .build();

            return new ValidatedCrudRequester<TransferResponse>(
                    Endpoint.TRANSFER_WITH_FRAUD_CHECK,
                    RequestSpecs.authAsUser(username, password),
                    ResponseSpecs.requestReturnsOK()).post(transferRequest);
        });
    }

    public TransferResponse transferNegativeWithFraudCheck(Long senderAccountId,
                                                           Long receiverAccountId,
                                                           Double amount,
                                                           String expectedErrorMessage) {
        return StepLogger.log("User " + username + " transfers " + amount + " to " + receiverAccountId + " with fraud check", () -> {
            TransferRequest transferRequest = TransferRequest.builder()
                    .senderAccountId(senderAccountId)
                    .receiverAccountId(receiverAccountId)
                    .amount(amount)
                    .build();

            // Получаем Response напрямую
            Response response = given()
                    .spec(RequestSpecs.authAsUser(username, password))
                    .contentType(ContentType.JSON)
                    .body(transferRequest)
                    .post(Endpoint.TRANSFER_WITH_FRAUD_CHECK.getUrl());

            // Проверяем вручную
            Assertions.assertThat(response.getStatusCode()).isEqualTo(400);
            Assertions.assertThat(response.getContentType()).contains("application/json");

            String errorMessage = response.getBody().asString();
            Assertions.assertThat(errorMessage).isEqualTo(expectedErrorMessage);

            // Возвращаем TransferResponse
            return TransferResponse.builder()
                    .message(errorMessage)
                    .build();
        });
    }
}
