package Iteration_2;

import Iteration_1.BaseTest;
import generators.RandomData;
import models.CustomerProfileResponse;
import models.TestAccData;
import models.UpdateProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.GetCustomerProfileRequester;
import requests.UpdateUserProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class ChangeNameProfile extends BaseTest {

    @Test
    public void UpdateCustomerProfile() {
        TestAccData user1 = TestDataFactory.createTransferTestData();

        CustomerProfileResponse user1Profile = new GetCustomerProfileRequester(RequestSpecs.authAsUser(user1.getUser1Username(), user1.getUser1Password()), ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(CustomerProfileResponse.class);

        String oldUserName = user1Profile.getName();
        Long oldUserId = user1Profile.getId();
        String newName = RandomData.getName();

        UpdateProfileResponse updateResponse = new UpdateUserProfileRequester(RequestSpecs.authAsUser(user1.getUser1Username(), user1.getUser1Password()), ResponseSpecs.requestReturnsOK())
                .put(newName)
                .extract()
                .as(UpdateProfileResponse.class);

        softly.assertThat(updateResponse.getMessage()).isEqualTo("Profile updated successfully");
        softly.assertThat(updateResponse.getCustomer().getName()).isEqualTo(newName);

        CustomerProfileResponse user1ProfileAfter = new GetCustomerProfileRequester(RequestSpecs.authAsUser(user1.getUser1Username(), user1.getUser1Password()), ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(CustomerProfileResponse.class);

        softly.assertThat(user1ProfileAfter.getName()).isEqualTo(newName);
        softly.assertThat(user1ProfileAfter.getId()).isEqualTo(oldUserId);

    }

    public static Stream<Arguments> invalidNamesValues() {
        return Stream.of(Arguments.of("Artem_art"),
                Arguments.of("Artem artem artem"),
                Arguments.of("Artem-artem"));
    }


    @MethodSource("invalidNamesValues")
    @ParameterizedTest
    public void UpdateCustomerProfileNameAtOneWord(String name) {

        TestAccData user1 = TestDataFactory.createTransferTestData();

        CustomerProfileResponse user1Profile = new GetCustomerProfileRequester(RequestSpecs.authAsUser(user1.getUser1Username(), user1.getUser1Password()), ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(CustomerProfileResponse.class);

        String oldUserName = user1Profile.getName();
        Long oldUserId = user1Profile.getId();

        new UpdateUserProfileRequester(RequestSpecs.authAsUser(user1.getUser1Username(), user1.getUser1Password()), ResponseSpecs.requestReturnsBadRequestWithText("Name must contain two words with letters only"))
                .put(name);

        CustomerProfileResponse user1ProfileAfter = new GetCustomerProfileRequester(RequestSpecs.authAsUser(user1.getUser1Username(), user1.getUser1Password()), ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(CustomerProfileResponse.class);

        softly.assertThat(user1ProfileAfter.getName()).isEqualTo(oldUserName);
        softly.assertThat(user1ProfileAfter.getId()).isEqualTo(oldUserId);
    }
}
