package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@EqualsAndHashCode(callSuper = true)
public class UserInfo extends BaseModel {
    private List<CreateUserResponse> users;

    public UserInfo() {
        this.users = new ArrayList<>();
    }
}
