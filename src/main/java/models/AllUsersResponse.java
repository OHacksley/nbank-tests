package models;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllUsersResponse extends BaseModel {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<AccountInfo> accounts;
}


