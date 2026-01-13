package ui;

import Iteration_1.ui.BaseUiTest;
import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.TransferPage;

import java.util.Optional;

import static api.models.DepositAmount.STANDARD_TRANSFER;

public class CreateUserTransfer extends BaseUiTest {

    @Test
    @UserSession(value = 2)
    public void transferWIthCorrectValue() {

        CreateUserRequest account1 = SessionStorage.getUser(1);

        CreateUserRequest account2 = SessionStorage.getUser(2);

        CreateAccountResponse account1Response = AdminSteps.createUserAccount(account1);

        Long account1Id = account1Response.getId();
        String acc1Numb = account1Response.getAccountNumber();

        CreateAccountResponse account2Response = AdminSteps.createUserAccount(account2);

        Long account2Id = account2Response.getId();
        String acc2Numb = account2Response.getAccountNumber();

        UserSteps senderStep = SessionStorage.getSteps(1);
        senderStep.makeDeposit(account1Id);
        senderStep.makeDeposit(account1Id);

        new TransferPage().open().sendTransfer(acc1Numb, account2.getUsername(), acc2Numb, STANDARD_TRANSFER)
                .chechAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage());

        CustomerProfileResponse getProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(
                        account2.getUsername(),
                        account2.getPassword()), ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        Optional<AccountResponse> targetArgument = getProfileResponse.getAccounts().stream()
                .filter(acc -> acc.getId().equals(account2Id))
                .findFirst();

        softly.assertThat(targetArgument.get().getBalance()).isEqualTo(DepositAmount.STANDARD_TRANSFER.getValue());

    }

    @Test
    @UserSession(value = 2)
    public void transferToNonExistAcc() {

        CreateUserRequest account1 = SessionStorage.getUser(1);

        CreateUserRequest account2 = SessionStorage.getUser(2);

        CreateAccountResponse account1Response = AdminSteps.createUserAccount(account1);

        Long account1Id = account1Response.getId();
        String acc1Numb = account1Response.getAccountNumber();

        CreateAccountResponse account2Response = AdminSteps.createUserAccount(account2);

        Long account2Id = account2Response.getId();
        String acc2Numb = account2Response.getAccountNumber();

        UserSteps senderStep = SessionStorage.getSteps(1);
        senderStep.makeDeposit(account1Id);
        senderStep.makeDeposit(account1Id);

        new TransferPage().open().sendNegativeTransfer(acc1Numb, STANDARD_TRANSFER)
                .chechAlertMessageAndAccept(BankAlert.NO_USER_FOUND.getMessage());

        CustomerProfileResponse getProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(
                        account2.getUsername(),
                        account2.getPassword()), ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        Optional<AccountResponse> targetArgument = getProfileResponse.getAccounts().stream()
                .filter(acc -> acc.getId().equals(account2Id))
                .findFirst();

        softly.assertThat(targetArgument.get().getBalance()).isZero();

    }


}
