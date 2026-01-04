package api.models;

public enum DepositAmount {
    STANDARD(5000.0),
    NEGATIVE(-0.1),
    ZERO(0.0),
    LARGE_DEPOSIT(5000.1),
    STANDARD_TRANSFER(10000.0),
    LARGE_TRANSFER(10000.1);


    private final Double amount;

    DepositAmount(Double amount) {
        this.amount = amount;
    }

    public Double getValue() {
        return amount;
    }
}
