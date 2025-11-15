package Iteration_2;

import Iteration_1.BaseTest;
import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;


public class ChangeNameProfile extends BaseTest {

    @Test
    public void UpdateCustomerProfile() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountResponse = AdminSteps.createUserAccount(userRequest);

        CustomerProfileResponse user1Profile = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        String oldUserName = user1Profile.getName();
        Long oldUserId = user1Profile.getId();

        UpdateProfileResponse updateResponse = new ValidatedCrudRequester<UpdateProfileResponse>(Endpoint.UPDATE_USER_PROFILE,
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .update(NewCustomerName.builder().newName(RandomData.getName()).build());

        softly.assertThat(updateResponse.getMessage()).isEqualTo(Message_And_Errors_text.PROFILE_UPDATED.getValue());
        softly.assertThat(updateResponse.getCustomer().getName()).isNotEqualTo(oldUserName);

        CustomerProfileResponse user1ProfileAfter = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        softly.assertThat(user1ProfileAfter.getName()).isEqualTo(updateResponse.getCustomer().getName());
        softly.assertThat(user1ProfileAfter.getId()).isEqualTo(oldUserId);

    }

    public static Stream<Arguments> invalidNamesValues() {
        return Stream.of(Arguments.of("Artem_art"),
                Arguments.of("Artem artem artem"),
                Arguments.of("Artem-artem"));
    }


    @MethodSource("invalidNamesValues")
    @ParameterizedTest
    public void UpdateCustomerProfileNameWithInvalidData(String name) {

        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountResponse = AdminSteps.createUserAccount(userRequest);


        CustomerProfileResponse user1Profile = new ValidatedCrudRequester<CustomerProfileResponse>(Endpoint.CUSTOMER_PROFILE,
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .getWithoutId();

        String oldUserName = user1Profile.getName();
        Long oldUserId = user1Profile.getId();

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
    }
}
