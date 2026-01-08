package api.models;

public enum TypeOfOperations {
    DEPOSIT("DEPOSIT"),
    TRANSFER("TRANSFER")
    ;

    private final String Value;

    TypeOfOperations(String Value) {
        this.Value = Value;
    }

    public String getValue() {
        return Value;
    }


}

