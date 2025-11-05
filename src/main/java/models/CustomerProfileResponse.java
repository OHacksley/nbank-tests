package models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerProfileResponse {

    private Long id;
    private String username;
    private String message;
    private String password;
    private String name;
    private String role;
    private List<AccountInfo> accounts;
}
