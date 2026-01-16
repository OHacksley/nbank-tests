package Iteration_2.api;

import Iteration_1.api.BaseTest;
import api.dao.AccountDao;
import api.generators.RandomData;
import api.models.*;
import api.requests.steps.DataBaseSteps;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminAPISteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ChangeNameProfileTest extends BaseTest {


    @Disabled
    @Test
    public void UpdateCustomerProfile() {
        CreateUserRequest userRequest = AdminAPISteps.createUser();

        CreateAccountResponse accountResponse = AdminAPISteps.createUserAccount(userRequest);

        CustomerProfileResponse user1Profile = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        String oldUserName = user1Profile.getName();
        Long oldUserId = user1Profile.getId();
        String newUserName = RandomData.getName();
        UpdateProfileResponse updateResponse = new ValidatedCrudRequester<UpdateProfileResponse>(Endpoint.UPDATE_USER_PROFILE,
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .update(NewCustomerName.builder().newName(newUserName).build());

        softly.assertThat(updateResponse.getMessage()).isEqualTo(Message_And_Errors_text.PROFILE_UPDATED.getValue());
        softly.assertThat(updateResponse.getCustomer().getName()).isNotEqualTo(oldUserName);

        CustomerProfileResponse user1ProfileAfter = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        softly.assertThat(user1ProfileAfter.getName()).isEqualTo(updateResponse.getCustomer().getName());
        softly.assertThat(user1ProfileAfter.getId()).isEqualTo(oldUserId);

        assertThat(DataBaseSteps.getUserById(user1ProfileAfter.getId()).getName()).isEqualTo(newUserName);

    }

    public static Stream<Arguments> invalidNamesValues() {
        return Stream.of(Arguments.of("Artem_art"),
                Arguments.of("Artem artem artem"),
                Arguments.of("Artem-artem"));
    }

    @Disabled
    @MethodSource("invalidNamesValues")
    @ParameterizedTest
    public void UpdateCustomerProfileNameWithInvalidData(String name) {

        CreateUserRequest userRequest = AdminAPISteps.createUser();

        CreateAccountResponse accountResponse = AdminAPISteps.createUserAccount(userRequest);


        CustomerProfileResponse user1Profile = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        String oldUserName = user1Profile.getName();
        Long oldUserId = user1Profile.getId();
        String newUserName = RandomData.getName();

        NewCustomerName newName = NewCustomerName.builder().newName(name).build();

        UpdateProfileResponse user1ProfileAfter = new ValidatedCrudRequester<UpdateProfileResponse>(Endpoint.UPDATE_USER_PROFILE,
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(Message_And_Errors_text.PROFILE_INVALID_NAME.getValue()))
                .update(newName);

        CustomerProfileResponse userAfterProfile = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        softly.assertThat(userAfterProfile.getName()).isEqualTo(oldUserName);
        softly.assertThat(userAfterProfile.getId()).isEqualTo(oldUserId);

        assertThat(DataBaseSteps.getUserById(user1Profile.getId()).getName()).isNull();

    }
}
