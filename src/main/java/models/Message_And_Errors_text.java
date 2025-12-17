package models;

public enum Message_And_Errors_text {
    DEPOSIT_FORBIDDEN("Unauthorized access to account"),
    DEPOSIT_ACC_NOT_FOUND("Account not found"),
    TRANSFER_SUCCES("Transfer successful"),
    TRANSFER_LEAST("Transfer amount must be at least 0.01"),
    TRANSFER_EXCEED("Transfer amount cannot exceed 10000"),
    TRANSFER_INVALID("Invalid transfer: insufficient funds or invalid accounts"),
    PROFILE_UPDATED("Profile updated successfully"),
    PROFILE_INVALID_NAME("Name must contain two words with letters only");

    private final String Value;

    Message_And_Errors_text(String Value) {
        this.Value = Value;
    }

    public String getValue() {
        return Value;
    }

}
