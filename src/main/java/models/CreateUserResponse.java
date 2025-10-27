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
public class CreateUserResponse extends BaseModel {
    private long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<String> accounts;
}
/*
    {
        "id": 1,
            "username": "testTet",
            "password": "$2a$10$XSOb1YyPhrv0SoZHAEkHC.q1V4mGqsATYIuT7onlCw74hVXUZMmfC",
            "name": null,
            "role": "USER",
            "accounts": []
    }

 */

