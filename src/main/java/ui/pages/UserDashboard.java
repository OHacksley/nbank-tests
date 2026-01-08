package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.assertj.core.internal.Conditions;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
@Getter
public class UserDashboard extends BasePage<UserDashboard> {

private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    public UserDashboard waitWelcomeText (String expectedText) {

        welcomeText.shouldNotHave(text("noname"));
        Duration.ofSeconds(2);
        welcomeText.shouldBe(visible, Duration.ofSeconds(3))
                .shouldHave(text(expectedText));
        return this;
    }

}
