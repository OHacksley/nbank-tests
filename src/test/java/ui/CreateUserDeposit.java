package ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.DepositAmount;
import models.LoginUserRequest;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Configuration.baseUrl;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static models.DepositAmount.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateUserDeposit {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        baseUrl = "http://192.168.1.101:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enablelog", true));
    }

    @Test
    public void userCreateDeposit() {
        //ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ (на уровне API)
        //ШАГ 1 : админ логинится в банке
        //ШАГ 2 Админ создает юзера
        //ШАГ 3 юзер логинится в банке

        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(Endpoint.LOGIN,
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        //ШАГИ ТЕСТА
        //ШАГ 4 юзер создает аккаунт

        $(Selectors.byText("➕ Create New Account")).click();

        //ШАГ 5 : Проверка создания на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ New Account Created! Account Number:");

        alert.accept();

        Pattern pattern = Pattern.compile("Account Number: (\\w+)");
        Matcher matcher = pattern.matcher(alertText);

        matcher.find();

        String createdAccNumber = matcher.group(1);

        // ШАГ 6: Аккаунт был создан на API

        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .extract().as(CreateAccountResponse[].class);

        assertThat(existingUserAccounts).hasSize(1);


        CreateAccountResponse createdAccount = existingUserAccounts[0];

        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getBalance()).isZero();

        //ШАГ 7 : открываем страницу Депозита
        //Selenide.open("/dashboard");
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

    String currentUrl = webdriver().object().getCurrentUrl();
    assertThat(currentUrl).isEqualTo(baseUrl + "/deposit");
        $(".container.mt-4.text-center h1").shouldHave(text("💰 Deposit Money"));

        //ШАГ 8 : выбираем селектор и вводим сумму депозита

        $("select.form-control.account-selector")
                .selectOptionContainingText(createdAccNumber);

        String selectedValue = $("select.form-control.account-selector")
                .getSelectedOption()
                .getText();

        assertThat(selectedValue).contains(createdAccNumber);

        SelenideElement amountField = $("input.form-control.deposit-input");
        amountField.clear();
        amountField.sendKeys(String.valueOf(STANDARD.getValue()));
        $x("//button[contains(text(), 'Deposit')]").click();

        Alert alert2 = switchTo().alert();
        String alert2Text = alert2.getText();
        alert2.accept();

        Pattern pattern2 = Pattern.compile("✅ Successfully deposited \\$([0-9]+(?:\\.[0-9]+)?) to account (\\w+)!");
        Matcher matcher2 = pattern2.matcher(alert2Text);

        matcher2.find();

        String actualAmount = matcher2.group(1);
        String actualAccount = matcher2.group(2);

        assertThat(Double.parseDouble(actualAmount)).isEqualTo(STANDARD.getValue());

        //ШАГ 9 : проверяем баланс на уровне API

        CreateAccountResponse[] existingUserAccounts2 = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse checkBalance = existingUserAccounts2[0];

        assertThat(checkBalance).isNotNull();
        assertThat(checkBalance.getBalance()).isEqualTo(STANDARD.getValue());

    }

    @Test
    public void depositInvalidValue() {
        //ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ (на уровне API)
        //ШАГ 1 : админ логинится в банке
        //ШАГ 2 Админ создает юзера
        //ШАГ 3 юзер логинится в банке

        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(Endpoint.LOGIN,
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        //ШАГИ ТЕСТА
        //ШАГ 4 юзер создает аккаунт

        $(Selectors.byText("➕ Create New Account")).click();

        //ШАГ 5 : Проверка создания на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ New Account Created! Account Number:");

        alert.accept();

        Pattern pattern = Pattern.compile("Account Number: (\\w+)");
        Matcher matcher = pattern.matcher(alertText);

        matcher.find();

        String createdAccNumber = matcher.group(1);

        // ШАГ 6: Аккаунт был создан на API

        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .extract().as(CreateAccountResponse[].class);

        assertThat(existingUserAccounts).hasSize(1);


        CreateAccountResponse createdAccount = existingUserAccounts[0];

        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getBalance()).isZero();

        //ШАГ 7 : открываем страницу Депозита
        //Selenide.open("/dashboard");
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        String currentUrl = webdriver().object().getCurrentUrl();
        assertThat(currentUrl).isEqualTo(baseUrl + "/deposit");
        $(".container.mt-4.text-center h1").shouldHave(text("💰 Deposit Money"));

        //ШАГ 8 : выбираем селектор и вводим сумму депозита

        $("select.form-control.account-selector")
                .selectOptionContainingText(createdAccNumber);

        String selectedValue = $("select.form-control.account-selector")
                .getSelectedOption()
                .getText();

        assertThat(selectedValue).contains(createdAccNumber);

        SelenideElement amountField = $("input.form-control.deposit-input");
        amountField.clear();
        amountField.sendKeys(String.valueOf(NEGATIVE.getValue()));
        $x("//button[contains(text(), 'Deposit')]").click();

        Alert alert2 = switchTo().alert();
        String alert2Text = alert2.getText();

        assertThat(alert2Text).isEqualTo("❌ Please enter a valid amount.");
        alert2.accept();

        //ШАГ 9 : проверяем баланс на уровне API

        CreateAccountResponse[] existingUserAccounts2 = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse checkBalance = existingUserAccounts2[0];

        assertThat(checkBalance).isNotNull();
        assertThat(checkBalance.getBalance()).isZero();

    }

}
