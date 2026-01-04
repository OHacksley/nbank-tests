package Iteration_1.ui;

import api.requests.steps.AdminSteps;
import com.codeborne.selenide.*;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static com.codeborne.selenide.Selenide.$;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateUserTest extends BaseUiTest {

@Test
public void adminCanCreateUserTest () {
//ШАГ 1 : админ залогинился в банке
    CreateUserRequest admin = CreateUserRequest.getAdmin();

    authAsUser(admin);
    //ШАГ2 : админ создает юзера в банке

    CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

    //ШАГ 3 : проверка текста алерта

    new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
            .chechAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
            .getAllUsers().findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);

    //ШАГ 5 : проверка, что юзер создан на API

CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
        .filter(user ->user.getUsername().equals(newUser.getUsername()))
        .findFirst().get();

    ModelAssertions.assertThatModels(newUser, createdUser).match();

}

@Test
public void adminCanNotCreateWithInvalidDataTest() {
    //ШАГ 1 : админ залогинился в банке
    CreateUserRequest admin = CreateUserRequest.getAdmin();

    authAsUser(admin);
    //ШАГ2 : админ создает юзера в банке

    CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
    newUser.setUsername("a");

    new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
            .chechAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
            .getAllUsers().findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.exist);

    //ШАГ 5 : проверка, что юзер не создан на API

    long userWithSameUsernameAsNewUser = AdminSteps.getAllUsers().stream()
            .filter(user ->user.getUsername().equals(newUser.getUsername())).count();

    assertThat(userWithSameUsernameAsNewUser).isZero();

}
}
