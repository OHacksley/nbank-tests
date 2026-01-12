package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ElementNotFound;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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

    public UserDashboard waitWelcomeText(String expectedText) {
        await().pollInSameThread()
                .atMost(4, TimeUnit.SECONDS)
                .ignoreException(ElementNotFound.class)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(welcomeText.getText().contains(expectedText)));
        return this;
    }


    public UserDashboard waitStandardWelcomeText() {
        String defaultText = "noname!";
        await().pollInSameThread()
                .atMost(4, TimeUnit.SECONDS)
                .ignoreException(ElementNotFound.class)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> welcomeText.getText().contains(defaultText));
        return this;
    }
}
