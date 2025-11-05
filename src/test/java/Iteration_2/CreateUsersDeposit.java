package Iteration_2;

import Iteration_1.BaseTest;
import generators.RandomData;
import io.restassured.http.ContentType;
import models.*;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.GetCustomerProfileRequester;
import requests.UserCreateDepositRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Optional;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateUsersDeposit extends BaseTest {

    @Test
    public void UsersDepositCorrectSum() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest).extract().as(CreateUserResponse.class);

        CreateAccountResponse accountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(
                userRequest.getUsername(),
                userRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        Long accountId = accountResponse.getId();
        String accountNumber = accountResponse.getAccountNumber();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accountId)
                .balance(DepositAmount.STANDARD.getValue())
                .build();

        DepositResponse depositResponse = new UserCreateDepositRequester(RequestSpecs.authAsUser(
                userRequest.getUsername(),
                userRequest.getPassword()), ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(DepositResponse.class);
        //Проверки "основного" тела
        softly.assertThat(depositResponse.getId()).isEqualTo(accountId);
        softly.assertThat(depositResponse.getAccountNumber()).isEqualTo(accountNumber);
        softly.assertThat(depositResponse.getBalance()).isEqualTo(DepositAmount.STANDARD.getValue());
        //Проверки блоков "Транзакции"
        softly.assertThat(depositResponse.getTransactions()).isNotEmpty();
        softly.assertThat(depositResponse.getTransactions().get(0).getAmount()).isEqualTo(DepositAmount.STANDARD);
        softly.assertThat(depositResponse.getTransactions().get(0).getType()).isEqualTo(TypeOfOperations.DEPOSIT);

    }

    public static Stream<Arguments> depositInvalidValues() {
        return Stream.of(Arguments.of(-0.1, "Deposit amount must be at least 0.01"),
                Arguments.of(0.0, "Deposit amount must be at least 0.01"),
                Arguments.of(5000.1, "Deposit amount cannot exceed 5000"));
    }

    @MethodSource("depositInvalidValues")
    @ParameterizedTest
    public void depositWithInvalidData(double balance, String errorValue) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest).extract().as(CreateUserResponse.class);

        CreateAccountResponse accountResponse = new CreateAccountRequester(RequestSpecs.authAsUser(
                userRequest.getUsername(),
                userRequest.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        Long accountId = accountResponse.getId();
        String accountNumber = accountResponse.getAccountNumber();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        new UserCreateDepositRequester(RequestSpecs.authAsUser(
                userRequest.getUsername(),
                userRequest.getPassword()), ResponseSpecs.requestReturnsBadRequestWithText(errorValue))
                .post(depositRequest);

        CustomerProfileResponse getProfileResponse = new GetCustomerProfileRequester(RequestSpecs.authAsUser(
                userRequest.getUsername(),
                userRequest.getPassword()), ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(CustomerProfileResponse.class);

        Optional<AccountInfo> targetArgument = getProfileResponse.getAccounts().stream()
                .filter(acc -> acc.getId().equals(accountId))
                .findFirst();
        softly.assertThat(targetArgument.get().getBalance()).isEqualTo(0.0);
    }


        @Test
        public void depositToForeignAcc() {

        // Создаем 1ого пользователя (аккаунт)

            CreateUserRequest account1 = CreateUserRequest.builder()
                    .username(RandomData.getUsername())
                    .password(RandomData.getPassword())
                    .role(UserRole.USER.toString())
                    .build();

            CreateUserResponse createUser1Response = new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                    ResponseSpecs.entityWasCreated())
                    .post(account1).extract().as(CreateUserResponse.class);

            // Создаем 2ого пользователя (аккаунт)

            CreateUserRequest account2 = CreateUserRequest.builder()
                    .username(RandomData.getUsername())
                    .password(RandomData.getPassword())
                    .role(UserRole.USER.toString())
                    .build();

            CreateUserResponse createUser2Response = new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                    ResponseSpecs.entityWasCreated())
                    .post(account2).extract().as(CreateUserResponse.class);

            // Создаем счет для второго пользователя и получаем его id

            CreateAccountResponse account2Response = new CreateAccountRequester(RequestSpecs.authAsUser(
                    account2.getUsername(),
                    account2.getPassword()), ResponseSpecs.entityWasCreated())
                    .post(null)
                    .extract()
                    .as(CreateAccountResponse.class);

            Long account2Id = account2Response.getId();

            //Логинимся под 1 пользователем и выполняем депозит на второй аккаунт
            DepositRequest depositRequest = DepositRequest.builder()
                    .id(account2Id)
                    .balance(DepositAmount.STANDARD.getValue())
                    .build();

            new UserCreateDepositRequester(RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()), ResponseSpecs.requestReturnsForbiddenWithText("Unauthorized access to account"))
                    .post(depositRequest);

            //Логинимся под вторым аккаунтом

            CustomerProfileResponse getProfileResponse = new GetCustomerProfileRequester(RequestSpecs.authAsUser(
                    account2.getUsername(),
                    account2.getPassword()), ResponseSpecs.requestReturnsOK())
                    .get()
                    .extract()
                    .as(CustomerProfileResponse.class);

            //Проверяем баланс 2ого аккаунта

            Optional<AccountInfo> targetArgument = getProfileResponse.getAccounts().stream()
                    .filter(acc -> acc.getId().equals(account2Id))
                    .findFirst();
            softly.assertThat(targetArgument.get().getBalance()).isEqualTo(0L);
    }

    @Test
    public void depositToIncorrectAcc() {
        // Создаем 1ого пользователя (аккаунт)

        CreateUserRequest account1 = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUser1Response = new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(account1).extract().as(CreateUserResponse.class);

        DepositRequest depositRequest = DepositRequest.builder()
                .id(0L)
                .balance(DepositAmount.STANDARD.getValue())
                .build();

        new UserCreateDepositRequester(RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()), ResponseSpecs.requestReturnsBadRequestWithText("Account not found"))
                .post(depositRequest);

    }
}


