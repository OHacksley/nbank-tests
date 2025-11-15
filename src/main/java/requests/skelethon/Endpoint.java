package requests.skelethon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER("/admin/users", CreateUserRequest.class, CreateUserResponse.class),
    ACCOUNTS("/accounts", BaseModel.class, CreateAccountResponse.class),
    LOGIN("/auth/login", BaseModel.class, LoginUserResponse.class),
    DEPOSIT("/accounts/deposit", DepositRequest.class, DepositResponse.class),
    CUSTOMER_PROFILE("/customer/profile", BaseModel.class, CustomerProfileResponse.class),
    TRANSFER("/accounts/transfer", TransferRequest.class, TransferResponse.class),
    UPDATE_USER_PROFILE("/customer/profile", UpdateProfileNameRequest.class, UpdateProfileResponse.class),
    GET_ALL_USERS("/admin/users",null, AllUsersResponse.class);

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
