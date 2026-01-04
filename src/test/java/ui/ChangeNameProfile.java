package ui;

import Iteration_1.ui.BaseUiTest;
import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.ProfilePage;
import ui.pages.UserDashboard;

import static com.codeborne.selenide.Condition.text;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeNameProfile extends BaseUiTest {

    @Test
    public void changeNameWithCorrectData() {

        // Создаем 1ого пользователя (аккаунт)

        CreateUserRequest user1 = AdminSteps.createUser();

        // Создаем счет для первого пользователя и получаем его id

        CreateAccountResponse account1Response = AdminSteps.createUserAccount(user1);

        authAsUser(user1);
        String newName = RandomData.getName();

        new ProfilePage().open().changeName(newName);
        new ProfilePage().chechAlertMessageAndAccept(BankAlert.NAME_UPDATE_SUCCESSFULLY.getMessage());
        new UserDashboard().open().waitWelcomeText(newName);

        Selenide.sleep(3000);
        UserSteps userSteps = new UserSteps(user1.getUsername(), user1.getPassword());
        assertThat(userSteps.getProfileInfo().getName()).isEqualTo(newName);

    }

    @Test
    public void changeNameWithIncorrectData() {

// Создаем 1ого пользователя (аккаунт)

        CreateUserRequest user1 = AdminSteps.createUser();

        // Создаем счет для первого пользователя и получаем его id

        CreateAccountResponse account1Response = AdminSteps.createUserAccount(user1);

        authAsUser(user1);
        String newName = RandomData.getIncorrectProfileName();

        new ProfilePage().open().changeName(newName);
        new ProfilePage().chechAlertMessageAndAccept(BankAlert.INCORRECT_PROFILE_NAME.getMessage());
        new UserDashboard().open()
                .getWelcomeText().shouldNotHave(text(newName))
                .shouldHave(text("noname"));


        Selenide.sleep(3000);
        UserSteps userSteps = new UserSteps(user1.getUsername(), user1.getPassword());
        assertThat(userSteps.getProfileInfo().getName()).isNull();

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