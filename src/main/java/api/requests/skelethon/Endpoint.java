package api.requests.skelethon;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER("/admin/users", CreateUserRequest.class, CreateUserResponse.class),
    ACCOUNTS("/accounts", BaseModel.class, CreateAccountResponse.class),
    LOGIN("/auth/login", BaseModel.class, LoginUserResponse.class),
    ACCOUNT_DEPOSIT("/accounts/deposit", DepositRequest.class, DepositResponse.class),
    CUSTOMER_PROFILE("/customer/profile", BaseModel.class, CustomerProfileResponse.class),
    TRANSFER("/accounts/transfer", TransferRequest.class, TransferResponse.class),
    UPDATE_USER_PROFILE("/customer/profile", UpdateProfileNameRequest.class, UpdateProfileResponse.class),
    GET_ALL_USERS("/admin/users",null, AllUsersResponse.class),
    CUSTOMER_ACCOUNTS("/customer/accounts", BaseModel.class, CreateAccountResponse.class),
    TRANSFER_WITH_FRAUD_CHECK("/accounts/transfer-with-fraud-check", TransferRequest.class, TransferResponse.class),
    FRAUD_CHECK_STATUS("/api/v1/accounts/fraud-check/{transactionId}", BaseModel.class, TransferFraudResponse.class);



    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
