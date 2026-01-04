package ui.pages;

import api.models.DepositAmount;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static org.assertj.core.api.Assertions.assertThat;

@Getter
public class DepositPage extends BasePage<DepositPage>{

    SelenideElement welcomeDepositText = $(".container.mt-4.text-center h1");
    SelenideElement selectorAcc = $("select.form-control.account-selector");
            //.selectOptionContainingText(createdAccNumber);
    SelenideElement amountField = $("input.form-control.deposit-input");
    SelenideElement buttonDeposit = $x("//button[contains(text(), 'Deposit')]");


    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage makeDeposit(String accNumber, DepositAmount depositAmount){
        $("select.form-control.account-selector")
                .selectOptionContainingText(accNumber);

        String selectedValue = $("select.form-control.account-selector")
                .getSelectedOption()
                .getText();

        SelenideElement amountField = $("input.form-control.deposit-input");
        amountField.clear();
        amountField.sendKeys(String.valueOf(depositAmount.getValue()));
        $x("//button[contains(text(), 'Deposit')]").click();
        return this;
    }

}
