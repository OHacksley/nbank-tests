package Iteration_2.api;

import Iteration_1.api.BaseTest;
import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static Iteration_2.api.TransfersDataHelper.USER_1_ID;
import static Iteration_2.api.TransfersDataHelper.USER_2_ID;

public class CreateUserTransfers extends BaseTest {

    @Test
    public void tranfserValidSumm() {

        // Создаем 1ого пользователя (аккаунт)

        CreateUserRequest account1 = AdminSteps.createUser();

        // Создаем 2ого пользователя (аккаунт)

        CreateUserRequest account2 = AdminSteps.createUser();

        // Создаем счет для первого пользователя и получаем его id

        CreateAccountResponse account1Response = AdminSteps.createUserAccount(account1);

        Long account1Id = account1Response.getId();

        // Создаем счет для второго пользователя и получаем его id

        CreateAccountResponse account2Response = AdminSteps.createUserAccount(account2);

        Long account2Id = account2Response.getId();

        //Логинимся под 1 пользователем и выполняем депозит на 10000
        DepositRequest depositRequest = DepositRequest.builder()
                .id(account1Id)
                .balance(DepositAmount.STANDARD.getValue())
                .build();

        new CrudRequester(Endpoint.DEPOSIT,
                RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        new CrudRequester(Endpoint.DEPOSIT,
                RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        //Совершаем трансфер с 1 аккаунта на 2ой
        // + промежуточные проверки успешного трансфера

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(account1Id)
                .receiverAccountId(account2Id)
                .amount(DepositAmount.STANDARD_TRANSFER.getValue())
                .build();

        TransferResponse transferResponse = new ValidatedCrudRequester<TransferResponse>(Endpoint.TRANSFER,
                RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest);

        softly.assertThat(transferResponse.getMessage()).isEqualTo(Message_And_Errors_text.TRANSFER_SUCCES.getValue());
        softly.assertThat(transferResponse.getAmount()).isEqualTo(DepositAmount.STANDARD_TRANSFER.getValue());

        //Логинимся под 2 пользователем, проверяем баланс

        CustomerProfileResponse getProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(
                        account2.getUsername(),
                        account2.getPassword()), ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        //Проверяем баланс 2ого аккаунта

        Optional<AccountResponse> targetArgument = getProfileResponse.getAccounts().stream()
                .filter(acc -> acc.getId().equals(account2Id))
                .findFirst();

        softly.assertThat(targetArgument.get().getBalance()).isEqualTo(DepositAmount.STANDARD_TRANSFER.getValue());
    }

    public static Stream<Arguments> transferInvalidValues() {
        return Stream.of(Arguments.of(USER_1_ID, USER_2_ID, DepositAmount.NEGATIVE.getValue(), Message_And_Errors_text.TRANSFER_LEAST.getValue()),
                Arguments.of(USER_1_ID, USER_2_ID, DepositAmount.ZERO.getValue(), Message_And_Errors_text.TRANSFER_LEAST.getValue()),
                Arguments.of(USER_1_ID, USER_2_ID, DepositAmount.LARGE_TRANSFER.getValue(), Message_And_Errors_text.TRANSFER_EXCEED.getValue()));
    }

    @MethodSource("transferInvalidValues")
    @ParameterizedTest
    public void transferWithInvalidData(Long User1ID, Long User2ID, Double amount, String errorValue) {

        CreateUserRequest account1 = AdminSteps.createUser();

        CreateAccountResponse account1Response = AdminSteps.createUserAccount(account1);

        Long account1Id = account1Response.getId();

        CreateUserRequest account2 = AdminSteps.createUser();

        CreateAccountResponse account2Response = AdminSteps.createUserAccount(account2);

        Long account2Id = account2Response.getId();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(account1Id)
                .balance(DepositAmount.STANDARD.getValue())
                .build();

        new CrudRequester(Endpoint.DEPOSIT,
                RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .receiverAccountId(account2Id)
                .senderAccountId(account1Id)
                .amount(amount)
                .build();

        new CrudRequester(Endpoint.TRANSFER,
                RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(errorValue))
                .post(transferRequest);

        CustomerProfileResponse getProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        //Проверяем баланс 1ого аккаунта

        Optional<AccountResponse> targetArgument = getProfileResponse.getAccounts().stream()
                .filter(acc -> acc.getId().equals(account1Id))
                .findFirst();
        softly.assertThat(targetArgument.isPresent()).isTrue();
        softly.assertThat(targetArgument.get().getBalance()).isEqualTo(DepositAmount.STANDARD.getValue());

    }

    @Test
    public void transferWithBalanceLessTransferSumm() {
        CreateUserRequest account1 = AdminSteps.createUser();

        CreateAccountResponse account1Response = AdminSteps.createUserAccount(account1);

        Long account1Id = account1Response.getId();

        CreateUserRequest account2 = AdminSteps.createUser();

        CreateAccountResponse account2Response = AdminSteps.createUserAccount(account2);

        Long account2Id = account2Response.getId();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(account1Id)
                .balance(DepositAmount.STANDARD.getValue())
                .build();

        new CrudRequester(Endpoint.DEPOSIT,
                RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        TransferRequest transferRequest = TransferRequest.builder()
                .receiverAccountId(account2Id)
                .senderAccountId(account1Id)
                .amount(DepositAmount.STANDARD_TRANSFER.getValue())
                .build();

        new CrudRequester(Endpoint.TRANSFER,
                RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(Message_And_Errors_text.TRANSFER_INVALID.getValue()))
                .post(transferRequest);

        CustomerProfileResponse getProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(account1.getUsername(), account1.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        //Проверяем баланс 1ого аккаунта

        Optional<AccountResponse> targetArgument = getProfileResponse.getAccounts().stream()
                .filter(acc -> acc.getId().equals(account1Id))
                .findFirst();
        softly.assertThat(targetArgument.isPresent()).isTrue();
        softly.assertThat(targetArgument.get().getBalance()).isEqualTo(DepositAmount.STANDARD.getValue());

    }
}





