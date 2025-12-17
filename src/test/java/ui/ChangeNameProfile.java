package ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import generators.RandomData;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Configuration.baseUrl;
import static com.codeborne.selenide.Configuration.textCheck;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameProfile {

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
    public void changeNameWithCorrectData() {


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


        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byClassName("user-info")).click();

        String newName = RandomData.getName();

        $(byAttribute("placeholder", "Enter new name")).sendKeys(newName);

        $(byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("✅ Name updated successfully!");

        alert.accept();

        Selenide.open("/dashboard");

        SelenideElement welcomeText = $("h2.welcome-text");
        welcomeText.shouldHave(text(newName));

        CustomerProfileResponse user1ProfileAfter = given()
                .spec(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()))
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .extract().as(CustomerProfileResponse.class);

        String checkName = user1ProfileAfter.getName();
        System.out.println(checkName);

        assertThat(checkName).isEqualTo(newName);

    }

    @Test
    public void changeNameWithIncorrectData() {


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


        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byClassName("user-info")).click();

        String newName = RandomData.getName();

        $(byAttribute("placeholder", "Enter new name")).sendKeys("0");

        $(byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText).contains("Name must contain two words with letters only");

        alert.accept();

        Selenide.open("/dashboard");

        SelenideElement welcomeText = $("h2.welcome-text");
        welcomeText.shouldHave(text("Noname"));

        CustomerProfileResponse user1ProfileAfter = given()
                .spec(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()))
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .extract().as(CustomerProfileResponse.class);

        String checkName = user1ProfileAfter.getName();
        System.out.println(checkName);

        assertThat(checkName).isNull();

    }

}