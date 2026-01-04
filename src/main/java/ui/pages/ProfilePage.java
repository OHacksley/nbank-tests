package ui.pages;

import api.generators.RandomData;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class ProfilePage extends BasePage<ProfilePage> {
    @Override
    public String url() {
        return "/edit-profile";
    }

    SelenideElement inputNewName = $(byAttribute("placeholder", "Enter new name"));
    SelenideElement buttonSaveChanges = $(byText("\uD83D\uDCBE Save Changes"));

    public ProfilePage changeName(String newName) {
        inputNewName.clear();
        inputNewName.sendKeys(newName);
        buttonSaveChanges.click();
        return this;
    }
}
