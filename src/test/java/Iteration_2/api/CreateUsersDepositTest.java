package Iteration_2.api;

import Iteration_1.api.BaseTest;
import api.models.*;
import api.requests.steps.DataBaseSteps;
import common.extensions.ApiVersionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminAPISteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
@ExtendWith(ApiVersionExtension.class)
public class CreateUsersDepositTest extends BaseTest {

    @Test
    public void UsersDepositCorrectSum() {
        CreateUserRequest user1 = AdminAPISteps.createUser();
        CreateAccountResponse user1response = AdminAPISteps.createUserAccount(user1);

        Long accountId = user1response.getId();
        String accountNumber = user1response.getAccountNumber();

        DepositRequest depositRequest = DepositRequest.builder()
                .Id(accountId)
                .balance(DepositAmount.STANDARD.getValue())
                .build();

        DepositResponse depositResponse = new ValidatedCrudRequester<DepositResponse>(Endpoint.ACCOUNT_DEPOSIT, RequestSpecs.authAsUser(
                user1.getUsername(),
                user1.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);
        //Проверки "основного" тела
        softly.assertThat(depositResponse.getId()).isEqualTo(accountId);
        softly.assertThat(depositResponse.getAccountNumber()).isEqualTo(accountNumber);
        softly.assertThat(depositResponse.getBalance()).isEqualTo(DepositAmount.STANDARD.getValue());

        assertThat(DataBaseSteps.getAccountBalanceByAccountNumber(accountNumber)).isEqualTo(DepositAmount.STANDARD.getValue());

    }

    public static Stream<Arguments> depositInvalidValues() {
        return Stream.of(Arguments.of(-0.1, "Invalid account or amount"),
                Arguments.of(0.0, "Invalid account or amount"),
                Arguments.of(5000.1, "Deposit amount exceeds the 5000 limit"));
    }

    @MethodSource("depositInvalidValues")
    @ParameterizedTest
    public void depositWithInvalidData(double balance, String errorValue) {
        CreateUserRequest userRequest = AdminAPISteps.createUser();

        CreateAccountResponse accountResponse = AdminAPISteps.createUserAccount(userRequest);

        Long accountId = accountResponse.getId();
        String accountNumber = accountResponse.getAccountNumber();

        DepositRequest depositRequest = DepositRequest.builder()
                .Id(accountId)
                .balance(balance)
                .build();

        new CrudRequester(Endpoint.ACCOUNT_DEPOSIT, RequestSpecs.authAsUser(
                userRequest.getUsername(),
                userRequest.getPassword()), ResponseSpecs.requestReturnsBadRequestWithText(errorValue))
                .post(depositRequest);

        CustomerProfileResponse getProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(
                userRequest.getUsername(),
                userRequest.getPassword()), ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        softly.assertThat(getProfileResponse.getAccounts().get(0).getBalance()).isZero();
        assertThat(DataBaseSteps.getAccountBalanceByAccountNumber(accountNumber)).isZero();

    }


        @Test
        public void depositToForeignAcc() {
            CreateUserRequest userRequest = AdminAPISteps.createUser();
            CreateUserRequest userRequest2 = AdminAPISteps.createUser();

            CreateAccountResponse accountResponse = AdminAPISteps.createUserAccount(userRequest);
            CreateAccountResponse account2Response = AdminAPISteps.createUserAccount(userRequest2);

            Long accountId = accountResponse.getId();
            Long account2Id = account2Response.getId();

            DepositRequest depositRequest = DepositRequest.builder()
                    .Id(account2Id)
                    .balance(DepositAmount.STANDARD.getValue())
                    .build();

            new CrudRequester(Endpoint.ACCOUNT_DEPOSIT,
                    RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                    ResponseSpecs.requestReturnsForbiddenWithText(Message_And_Errors_text.DEPOSIT_FORBIDDEN.getValue()))
                    .post(depositRequest);

            CustomerProfileResponse checkBalance = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                    RequestSpecs.authAsUser(userRequest2.getUsername(),
                            userRequest2.getPassword()), ResponseSpecs.requestReturnsOK())
                    .getWithoutId();

            softly.assertThat(checkBalance.getAccounts().get(0).getBalance()).isEqualTo(0L);
            assertThat(DataBaseSteps.getAccountBalanceByAccountNumber(accountResponse.getAccountNumber())).isZero();


        }

    @Test
    public void depositToIncorrectAcc() {
        // Создаем 1ого пользователя (аккаунт)

        CreateUserRequest account1 = AdminAPISteps.createUser();
        CreateAccountResponse accountResponse = AdminAPISteps.createUserAccount(account1);

        DepositRequest depositRequest = DepositRequest.builder()
                .Id(0L)
                .balance(DepositAmount.STANDARD.getValue())
                .build();

        new CrudRequester(Endpoint.ACCOUNT_DEPOSIT,
                RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()),
                ResponseSpecs.requestReturnsForbiddenWithText(Message_And_Errors_text.DEPOSIT_FORBIDDEN.getValue()))
                .post(depositRequest);

        assertThat(DataBaseSteps.getAccountBalanceByAccountNumber(accountResponse.getAccountNumber())).isZero();

    }
}


