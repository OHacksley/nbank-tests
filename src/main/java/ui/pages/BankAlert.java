package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {

USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("username: Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number:"),
    DEPOSIT_SUCCESSFULLY("✅ Successfully deposited to account!"),
    INCORRECT_DEPOSIT_AMOUNT("❌ Please enter a valid amount."),
    TRANSFER_SUCCESSFULLY("✅ Successfully transferred \\$([0-9]+(?:\\.[0-9]+)?) to account (\\w+)!"),
    NO_USER_FOUND("❌ No user found with this account number."),
    NAME_UPDATE_SUCCESSFULLY("✅ Name updated successfully!"),
    INCORRECT_PROFILE_NAME("Name must contain two words with letters only");

private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
