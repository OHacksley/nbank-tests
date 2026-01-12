package ui;

import Iteration_1.ui.BaseUiTest;
import api.generators.RandomData;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.ProfilePage;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameProfile extends BaseUiTest {

    @Disabled
    @Test
    @UserSession
    public void changeNameWithCorrectData() {

        String newName = RandomData.getName();

        new ProfilePage().open().changeName(newName)
                .chechAlertMessageAndAccept(BankAlert.NAME_UPDATE_SUCCESSFULLY.getMessage());
        new UserDashboard().open().waitWelcomeText(newName);

        SessionStorage.getSteps().getProfileInfo(newName);

    }

    @Disabled
    @Test
    @UserSession
    public void changeNameWithIncorrectData() {

        String newName = RandomData.getIncorrectProfileName();

        new ProfilePage().open().changeName(newName)
                .chechAlertMessageAndAccept(BankAlert.INCORRECT_PROFILE_NAME.getMessage());
        new UserDashboard().open().waitStandardWelcomeText();

        assertThat(SessionStorage.getSteps().getProfileInfo("null").getName()).isNull();

    }

}

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

 */