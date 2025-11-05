package Iteration_2;

import Iteration_1.BaseTest;
import generators.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import models.*;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import static Iteration_2.TransfersDataHelper.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateUserTransfers extends BaseTest {

    @Test
    public void tranfserValidSumm() {

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

        // Создаем счет для первого пользователя и получаем его id

        CreateAccountResponse account1Response = new CreateAccountRequester(RequestSpecs.authAsUser(
                account1.getUsername(),
                account1.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        Long account1Id = account1Response.getId();

        // Создаем счет для второго пользователя и получаем его id

        CreateAccountResponse account2Response = new CreateAccountRequester(RequestSpecs.authAsUser(
                account2.getUsername(),
                account2.getPassword()), ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        Long account2Id = account2Response.getId();

        //Логинимся под 1 пользователем и выполняем депозит на 10000
        DepositRequest depositRequest = DepositRequest.builder()
                .id(account1Id)
                .balance(DepositAmount.STANDARD.getValue())
                .build();

        new UserCreateDepositRequester(RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()), ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        new UserCreateDepositRequester(RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()), ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        //Совершаем трансфер с 1 аккаунта на 2ой
        // + промежуточные проверки успешного трансфера

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(account1Id)
                .receiverAccountId(account2Id)
                .amount(DepositAmount.STANDARD_TRANSFER.getValue())
                .build();

        TransferResponse transferResponse = new UserCreateTransferRequester(RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()), ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract()
                .as(TransferResponse.class);

        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(transferResponse.getAmount()).isEqualTo(DepositAmount.STANDARD_TRANSFER.getValue());

        //Логинимся под 2 пользователем, проверяем баланс

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

        softly.assertThat(targetArgument.get().getBalance()).isEqualTo(DepositAmount.STANDARD_TRANSFER.getValue());
    }

    public static Stream<Arguments> transferInvalidValues() {
        return Stream.of(Arguments.of(USER_1_ID, USER_2_ID, DepositAmount.NEGATIVE.getValue(), "Transfer amount must be at least 0.01"),
                Arguments.of(USER_1_ID, USER_2_ID, DepositAmount.ZERO.getValue(), "Transfer amount must be at least 0.01"),
                Arguments.of(USER_1_ID, USER_2_ID, DepositAmount.LARGE_TRANSFER.getValue(), "Transfer amount cannot exceed 10000"));
    }

    @MethodSource("transferInvalidValues")
    @ParameterizedTest
    public void transferWithInvalidData(Long senderAccountId, Long receiverAccountId, Double amount, String errorValue) {

        TransfersDataHelper.depositToAccount(USER_1_ID, DepositAmount.STANDARD.getValue(), USER_1_USERNAME, USER_1_PASSWORD);
        TransfersDataHelper.depositToAccount(USER_1_ID, DepositAmount.STANDARD.getValue(), USER_1_USERNAME, USER_1_PASSWORD);

        TransferRequest transferRequest = TransferRequest.builder()
                .receiverAccountId(receiverAccountId)
                .senderAccountId(senderAccountId)
                .amount(amount)
                .build();

        new UserCreateTransferRequester(RequestSpecs.authAsUser(USER_1_USERNAME, USER_1_PASSWORD), ResponseSpecs.requestReturnsBadRequestWithText(errorValue))
                .post(transferRequest);

        CustomerProfileResponse getProfileResponse = new GetCustomerProfileRequester(RequestSpecs.authAsUser(USER_1_USERNAME, USER_1_PASSWORD), ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(CustomerProfileResponse.class);

        //Проверяем баланс 1ого аккаунта

        Optional<AccountInfo> targetArgument = getProfileResponse.getAccounts().stream()
                .filter(acc -> acc.getId().equals(USER_1_ID))
                .findFirst();
        softly.assertThat(targetArgument.isPresent()).isTrue();
        softly.assertThat(targetArgument.get().getBalance()).isEqualTo(DepositAmount.STANDARD_TRANSFER);

    }

    @Test
    public void transferWithBalanceLessTransferSumm() {
        //создаем 2 аккаунта и счета
        TestAccData testAccData = TestDataFactory.createTransferTestData();

        //Депозит на 1 акк 5000

        TransfersDataHelper.depositToAccount(testAccData.getAccount1Id(), DepositAmount.STANDARD.getValue(), testAccData.getUser1Username(), testAccData.getUser1Password());


TransferRequest transferRequest = TransferRequest.builder()
        .receiverAccountId(testAccData.getAccount2Id())
        .senderAccountId(testAccData.getAccount1Id())
        .amount(DepositAmount.STANDARD_TRANSFER.getValue())
        .build();

//Трансфер на 10000 и проврека ошибки

        new UserCreateTransferRequester(RequestSpecs.authAsUser(testAccData.getUser1Username(), testAccData.getUser1Password()), ResponseSpecs.requestReturnsBadRequestWithText("Invalid transfer: insufficient funds or invalid accounts"))
        .post(transferRequest);

        //проверка баланса акк 1

        CustomerProfileResponse profileResponse = new GetCustomerProfileRequester(RequestSpecs.authAsUser(testAccData.getUser1Username(), testAccData.getUser1Password()), ResponseSpecs.requestReturnsOK())
        .get()
        .extract()
        .as(CustomerProfileResponse.class);

Optional<AccountInfo> accInfo = profileResponse.getAccounts().stream()
        .filter(acc ->acc.getId().equals(testAccData.getAccount1Id()))
        .findFirst();

        softly.assertThat(accInfo.isPresent()).isTrue();
        softly.assertThat(accInfo.get().getBalance()).isEqualTo(DepositAmount.STANDARD.getValue());
    }
}





