package models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestAccData {

    private Long account1Id;
    private Long account2Id;
    private String user1Username;
    private String user1Password;
    private String user2Username;
    private String user2Password;

}

