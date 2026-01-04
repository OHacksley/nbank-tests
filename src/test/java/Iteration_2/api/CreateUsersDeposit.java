package Iteration_2.api;

import Iteration_1.api.BaseTest;
import api.models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateUsersDeposit extends BaseTest {

    @Test
    public void UsersDepositCorrectSum() {
        CreateUserRequest user1 = AdminSteps.createUser();
        CreateAccountResponse user1response = AdminSteps.createUserAccount(user1);

        Long accountId = user1response.getId();
        String accountNumber = user1response.getAccountNumber();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accountId)
                .balance(DepositAmount.STANDARD.getValue())
                .build();

        DepositResponse depositResponse = new ValidatedCrudRequester<DepositResponse>(Endpoint.DEPOSIT, RequestSpecs.authAsUser(
                user1.getUsername(),
                user1.getPassword()), ResponseSpecs.requestReturnsOK())
                .post(depositRequest);
        //Проверки "основного" тела
        softly.assertThat(depositResponse.getId()).isEqualTo(accountId);
        softly.assertThat(depositResponse.getAccountNumber()).isEqualTo(accountNumber);
        softly.assertThat(depositResponse.getBalance()).isEqualTo(DepositAmount.STANDARD.getValue());
        //Проверки блоков "Транзакции"
        softly.assertThat(depositResponse.getTransactions()).isNotEmpty();
        softly.assertThat(depositResponse.getTransactions().get(0).getAmount()).isEqualTo(DepositAmount.STANDARD.getValue());
        softly.assertThat(depositResponse.getTransactions().get(0).getType()).isEqualTo(TypeOfOperations.DEPOSIT.getValue());

    }

    public static Stream<Arguments> depositInvalidValues() {
        return Stream.of(Arguments.of(-0.1, "Deposit amount must be at least 0.01"),
                Arguments.of(0.0, "Deposit amount must be at least 0.01"),
                Arguments.of(5000.1, "Deposit amount cannot exceed 5000"));
    }

    @MethodSource("depositInvalidValues")
    @ParameterizedTest
    public void depositWithInvalidData(double balance, String errorValue) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountResponse = AdminSteps.createUserAccount(userRequest);

        Long accountId = accountResponse.getId();
        String accountNumber = accountResponse.getAccountNumber();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        new CrudRequester(Endpoint.DEPOSIT, RequestSpecs.authAsUser(
                userRequest.getUsername(),
                userRequest.getPassword()), ResponseSpecs.requestReturnsBadRequestWithText(errorValue))
                .post(depositRequest);

        CustomerProfileResponse getProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(
                userRequest.getUsername(),
                userRequest.getPassword()), ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        softly.assertThat(getProfileResponse.getAccounts().get(0).getBalance()).isEqualTo(0.0);
    }


        @Test
        public void depositToForeignAcc() {
            CreateUserRequest userRequest = AdminSteps.createUser();
            CreateUserRequest userRequest2 = AdminSteps.createUser();

            CreateAccountResponse accountResponse = AdminSteps.createUserAccount(userRequest);
            CreateAccountResponse account2Response = AdminSteps.createUserAccount(userRequest2);

            Long accountId = accountResponse.getId();
            Long account2Id = account2Response.getId();

            DepositRequest depositRequest = DepositRequest.builder()
                    .id(account2Id)
                    .balance(DepositAmount.STANDARD.getValue())
                    .build();

            new CrudRequester(Endpoint.DEPOSIT,
                    RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                    ResponseSpecs.requestReturnsForbiddenWithText(Message_And_Errors_text.DEPOSIT_FORBIDDEN.getValue()))
                    .post(depositRequest);

            CustomerProfileResponse checkBalance = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                    RequestSpecs.authAsUser(userRequest2.getUsername(),
                            userRequest2.getPassword()), ResponseSpecs.requestReturnsOK())
                    .getWithoutId();

            softly.assertThat(checkBalance.getAccounts().get(0).getBalance()).isEqualTo(0L);

    }

    @Test
    public void depositToIncorrectAcc() {
        // Создаем 1ого пользователя (аккаунт)

        CreateUserRequest account1 = AdminSteps.createUser();
        CreateAccountResponse accountResponse = AdminSteps.createUserAccount(account1);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(0L)
                .balance(DepositAmount.STANDARD.getValue())
                .build();

        new CrudRequester(Endpoint.DEPOSIT,
                RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(Message_And_Errors_text.DEPOSIT_ACC_NOT_FOUND.getValue()))
                .post(depositRequest);

    }
}


