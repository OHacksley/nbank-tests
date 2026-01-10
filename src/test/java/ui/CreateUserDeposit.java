package ui;

import Iteration_1.ui.BaseUiTest;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.DepositAmount;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateUserDeposit extends BaseUiTest {

    @Test
    @UserSession
    public void userCreateDeposit() {

        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps().getAllAccounts();

        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().chechAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();

        new DepositPage().open().makeDeposit(createdAccounts.getFirst().getAccountNumber(), DepositAmount.STANDARD);

        new DepositPage().chechAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage());

        List<CreateAccountResponse> createdAccountsAfterDeposit = SessionStorage.getSteps().getAllAccounts();
        assertThat(createdAccountsAfterDeposit).hasSize(1);
        assertThat(createdAccountsAfterDeposit.getFirst().getBalance()).isEqualTo(DepositAmount.STANDARD.getValue());

    }

    /*

            Alert alert2 = switchTo().alert();
            String alert2Text = alert2.getText();
            alert2.accept();

            Pattern pattern2 = Pattern.compile("✅ Successfully deposited \\$([0-9]+(?:\\.[0-9]+)?) to account (\\w+)!");
            Matcher matcher2 = pattern2.matcher(alert2Text);

            matcher2.find();

            String actualAmount = matcher2.group(1);
            String actualAccount = matcher2.group(2);

            assertThat(Double.parseDouble(actualAmount)).isEqualTo(STANDARD.getValue());
    */
    @Test
    @UserSession
    public void depositInvalidValue() {

        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = SessionStorage.getSteps().getAllAccounts();

        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().chechAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();

        new DepositPage().open().makeDeposit(createdAccounts.getFirst().getAccountNumber(), DepositAmount.NEGATIVE);

        new DepositPage().chechAlertMessageAndAccept(BankAlert.INCORRECT_DEPOSIT_AMOUNT.getMessage());

        List<CreateAccountResponse> createdAccountsAfterDeposit = SessionStorage.getSteps().getAllAccounts();
        assertThat(createdAccountsAfterDeposit).hasSize(1);
        assertThat(createdAccountsAfterDeposit.getFirst().getTransactions().isEmpty());
        assertThat(createdAccountsAfterDeposit.getFirst().getBalance()).isZero();

    }
}
