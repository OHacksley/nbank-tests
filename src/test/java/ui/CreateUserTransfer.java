package ui;

import Iteration_1.ui.BaseUiTest;
import api.models.*;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Configuration.baseUrl;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.switchTo;
import static io.restassured.RestAssured.given;
import static api.models.DepositAmount.STANDARD;
import static api.models.DepositAmount.STANDARD_TRANSFER;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateUserTransfer extends BaseUiTest {

    @Test
    public void transferWIthCorrectValue() {

        // Создаем 1ого пользователя (аккаунт)

        CreateUserRequest account1 = AdminSteps.createUser();

        // Создаем 2ого пользователя (аккаунт)

        CreateUserRequest account2 = AdminSteps.createUser();

        // Создаем счет для первого пользователя и получаем его id

        CreateAccountResponse account1Response = AdminSteps.createUserAccount(account1);

        Long account1Id = account1Response.getId();
        String acc1Numb = account1Response.getAccountNumber();

        // Создаем счет для второго пользователя и получаем его id

        CreateAccountResponse account2Response = AdminSteps.createUserAccount(account2);

        Long account2Id = account2Response.getId();
        String acc2Numb = account2Response.getAccountNumber();


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

        authAsUser(account1);

        new TransferPage().open().sendTransfer(acc1Numb, account2.getUsername(), acc2Numb, STANDARD_TRANSFER);

        new TransferPage().chechAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage());

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

        // Создаем 1ого пользователя (аккаунт)
/*
        CreateUserRequest user1 = AdminSteps.createUser();

        String userAuthHeader = new CrudRequester(Endpoint.LOGIN,
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user1.getUsername()).password(user1.getPassword()).build())
                .extract()
                .header("Authorization");
        System.out.println("Токен для UI: " + userAuthHeader);
        CreateAccountResponse account1Response = AdminSteps.createUserAccount(user1);

        Long account1Id = account1Response.getId();
        String numbAcc = account1Response.getAccountNumber();

        // Создаем 2ого пользователя (аккаунт)

        CreateUserRequest user2 = AdminSteps.createUser();

        // Создаем счет для второго пользователя и получаем его id

        CreateAccountResponse account2Response = AdminSteps.createUserAccount(user2);

        Long account2Id = account2Response.getId();
        String numbAcc2 = account2Response.getAccountNumber();

        //Логинимся под 1 пользователем и выполняем депозит на 10000
        DepositRequest depositRequest = DepositRequest.builder()
                .id(account1Id)
                .balance(DepositAmount.STANDARD.getValue())
                .build();

        new CrudRequester(Endpoint.DEPOSIT,
                RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        new CrudRequester(Endpoint.DEPOSIT,
                RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/transfer");

        assertThat($(Selectors.byText("\uD83D\uDD04 Make a Transfer")));

        $("select.form-control.account-selector")
                .shouldBe(visible, enabled)
                .selectOptionContainingText(numbAcc);

        $(byAttribute("placeholder", "Enter recipient name")).sendKeys(user2.getUsername());

        $(byAttribute("placeholder", "Enter recipient account number"))
                .sendKeys(numbAcc2);

        $(byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(DepositAmount.STANDARD_TRANSFER.getValue()));

        $(byId("confirmCheck")).shouldBe(visible, enabled).click();

        $(byText("\uD83D\uDE80 Send Transfer")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        System.out.println(alertText);
        alert.accept();

        Pattern pattern = Pattern.compile("✅ Successfully transferred \\$([0-9]+(?:\\.[0-9]+)?) to account (\\w+)!");
        Matcher matcher = pattern.matcher(alertText);

        matcher.find();

        String actualAmount = matcher.group(1);
        String actualAccount = matcher.group(2);

        assertThat(Double.parseDouble(actualAmount)).isEqualTo(STANDARD_TRANSFER.getValue());

        //ШАГ 9 : проверяем баланс на уровне API

        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user2.getUsername(), user2.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse checkBalance = existingUserAccounts[0];

        assertThat(checkBalance).isNotNull();
        assertThat(checkBalance.getBalance()).isEqualTo(STANDARD_TRANSFER.getValue());
        assertThat(checkBalance.getAccountNumber()).isEqualTo(actualAccount);
    }
*/
        @Test
        public void transferToNonExistAcc () {

            // Создаем 1ого пользователя (аккаунт)

            CreateUserRequest account1 = AdminSteps.createUser();

            // Создаем 2ого пользователя (аккаунт)

            CreateUserRequest account2 = AdminSteps.createUser();

            // Создаем счет для первого пользователя и получаем его id

            CreateAccountResponse account1Response = AdminSteps.createUserAccount(account1);

            Long account1Id = account1Response.getId();
            String acc1Numb = account1Response.getAccountNumber();

            // Создаем счет для второго пользователя и получаем его id

            CreateAccountResponse account2Response = AdminSteps.createUserAccount(account2);

            Long account2Id = account2Response.getId();
            String acc2Numb = account2Response.getAccountNumber();


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

            authAsUser(account1);

            new TransferPage().open().sendTransfer(acc1Numb, "non-exist account number", "ACC0", STANDARD_TRANSFER);

            new TransferPage().chechAlertMessageAndAccept(BankAlert.NO_USER_FOUND.getMessage());

            CustomerProfileResponse getProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                    RequestSpecs.authAsUser(
                            account2.getUsername(),
                            account2.getPassword()), ResponseSpecs.requestReturnsOK())
                    .getWithoutId();

            //Проверяем баланс 2ого аккаунта

            Optional<AccountResponse> targetArgument = getProfileResponse.getAccounts().stream()
                    .filter(acc -> acc.getId().equals(account2Id))
                    .findFirst();

            softly.assertThat(targetArgument.get().getBalance()).isZero();

            /*
            // Создаем 1ого пользователя (аккаунт)

            CreateUserRequest user1 = AdminSteps.createUser();

            String userAuthHeader = new CrudRequester(Endpoint.LOGIN,
                    RequestSpecs.unauthSpec(),
                    ResponseSpecs.requestReturnsOK())
                    .post(LoginUserRequest.builder().username(user1.getUsername()).password(user1.getPassword()).build())
                    .extract()
                    .header("Authorization");
            System.out.println("Токен для UI: " + userAuthHeader);
            CreateAccountResponse account1Response = AdminSteps.createUserAccount(user1);

            Long account1Id = account1Response.getId();
            String numbAcc = account1Response.getAccountNumber();

            //Логинимся под 1 пользователем и выполняем депозит на 5000
            DepositRequest depositRequest = DepositRequest.builder()
                    .id(account1Id)
                    .balance(DepositAmount.STANDARD.getValue())
                    .build();

            new CrudRequester(Endpoint.DEPOSIT,
                    RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()),
                    ResponseSpecs.requestReturnsOK())
                    .post(depositRequest);

            Selenide.open("/");

            executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

            Selenide.open("/transfer");

            assertThat($(Selectors.byText("\uD83D\uDD04 Make a Transfer")));

            $("select.form-control.account-selector")
                    .shouldBe(visible, enabled)
                    .selectOptionContainingText(numbAcc);

            $(byAttribute("placeholder", "Enter recipient name")).sendKeys("non-existAcc");

            $(byAttribute("placeholder", "Enter recipient account number"))
                    .sendKeys("ACC0");

            $(byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(STANDARD.getValue()));

            $(byId("confirmCheck")).shouldBe(visible, enabled).click();

            $(byText("\uD83D\uDE80 Send Transfer")).click();

            Alert alert = switchTo().alert();
            String alertText = alert.getText();
            System.out.println(alertText);
            alert.accept();

            //Проверяем текст ошибки в алерте

            assertThat(alertText).isEqualTo("❌ No user found with this account number.");

            // проверяем баланс на уровне API

            CreateAccountResponse[] existingUserAccounts = given()
                    .spec(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()))
                    .get("http://localhost:4111/api/v1/customer/accounts")
                    .then()
                    .assertThat()
                    .extract().as(CreateAccountResponse[].class);

            CreateAccountResponse checkBalance = existingUserAccounts[0];

            assertThat(checkBalance).isNotNull();
            assertThat(checkBalance.getBalance()).isEqualTo(STANDARD.getValue());
            assertThat(checkBalance.getAccountNumber()).isEqualTo(numbAcc);


             */
        }

}
