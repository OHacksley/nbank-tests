package ui.pages;

import api.models.DepositAmount;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class TransferPage extends BasePage<TransferPage> {

    @Override
    public String url() {
        return "/transfer";
    }

    SelenideElement selectAcc = $("select.form-control.account-selector");
    SelenideElement recipientName = $(byAttribute("placeholder", "Enter recipient name"));
    SelenideElement recipientAccNumber = $(byAttribute("placeholder", "Enter recipient account number"));
    SelenideElement enterAmount = $(byAttribute("placeholder", "Enter amount"));
    SelenideElement confirmDetailsCheck = $(byId("confirmCheck"));
    SelenideElement buttonSendTransfer = $(byText("\uD83D\uDE80 Send Transfer"));


    public TransferPage sendTransfer(String accNumber, String Username, String acc2Number, DepositAmount transferAmount) {

        selectAcc.selectOptionContainingText(accNumber);

        recipientName.sendKeys(Username);

        recipientAccNumber.sendKeys(acc2Number);

        enterAmount.sendKeys(String.valueOf(transferAmount.getValue()));

        confirmDetailsCheck.click();

        buttonSendTransfer.click();

        return this;

    }

}
