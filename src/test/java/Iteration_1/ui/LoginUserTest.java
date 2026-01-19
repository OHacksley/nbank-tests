package Iteration_1.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import api.models.CreateUserRequest;
import common.annotations.Browsers;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminAPISteps;
import ui.pages.AdminPanel;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

import static com.codeborne.selenide.Selenide.$;

public class LoginUserTest extends BaseUiTest {

    @Test
    @Browsers({"chrome"})
    public void adminCanLoginWithCorrectDataTest(){

        CreateUserRequest admin = CreateUserRequest.getAdmin();

        new LoginPage().open().login(admin.getUsername(), admin.getPassword())
                .getPage(AdminPanel.class).getAdminPanelText().shouldBe(Condition.visible);
    }

    @Test
    public void userCanLoginWithCorrectDataTest() {

        CreateUserRequest user = AdminAPISteps.createUser();

        Selenide.open("/login");

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).getWelcomeText()
                .shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));
    }
}
