package Iteration_1.api;

import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminAPISteps;
import common.annotations.FraudCheckMock;
import common.extensions.TimingExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@Disabled
@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {

    private CreateUserRequest user1;
    private CreateUserRequest user2;
    private CreateAccountResponse account1;
    private CreateAccountResponse account2;
    private DepositResponse depositResponse;
    private TransferFraudResponse transferResponse;

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void testTransferWithFraudCheck() {
        user1 = AdminAPISteps.createUser();

        AccountSteps accountSteps1 = new AccountSteps(user1.getUsername(), user1.getPassword());
        account1 = accountSteps1.createAccount();

        depositResponse = accountSteps1.depositToAccount(account1.getId(), DepositAmount.STANDARD.getValue());

        user2 = AdminAPISteps.createUser();
        AccountSteps accountSteps2 = new AccountSteps(user2.getUsername(), user2.getPassword());
        account2 = accountSteps2.createAccount();

        Double transferAmount = 500.0;//Math.random() * (DepositAmount.STANDARD.getValue() - 0.1) + 0.1;
        TransferResponse transferResponse = accountSteps1.transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                transferAmount
        );

        softly.assertThat(transferResponse).isNotNull();


        TransferFraudResponse expectedResponse = TransferFraudResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(transferAmount)
                .senderAccountId(account1.getId())
                .receiverAccountId(account2.getId())
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    public static Stream<Arguments> transferFraudInvalidValues() {
        return Stream.of(Arguments.of(DepositAmount.NEGATIVE.getValue(), Message_And_Errors_text.TRANSFER_FRAUD_INVALID_AMOUNT.getValue()),
                Arguments.of(DepositAmount.ZERO.getValue(), Message_And_Errors_text.TRANSFER_FRAUD_INVALID_AMOUNT.getValue()),
                Arguments.of(DepositAmount.LARGE_TRANSFER.getValue(), Message_And_Errors_text.INSUFFICIENT_FOUNDS.getValue()));
    }


    @MethodSource("transferFraudInvalidValues")
    @ParameterizedTest
    public void transferWithInvalidData(Double amount, String errorValue) {
        user1 = AdminAPISteps.createUser();

        AccountSteps accountSteps1 = new AccountSteps(user1.getUsername(), user1.getPassword());
        account1 = accountSteps1.createAccount();

        depositResponse = accountSteps1.depositToAccount(account1.getId(), DepositAmount.STANDARD.getValue());

        user2 = AdminAPISteps.createUser();
        AccountSteps accountSteps2 = new AccountSteps(user2.getUsername(), user2.getPassword());
        account2 = accountSteps2.createAccount();

        TransferResponse transferResponse = accountSteps1.transferNegativeWithFraudCheck(
                account1.getId(),
                account2.getId(),
                amount,
                errorValue
        );

        softly.assertThat(transferResponse).isNotNull();
        softly.assertThat(transferResponse.getMessage()).isEqualTo(errorValue);
    }


        @AfterEach
        public void afterTest () {
            softly.assertAll();
        }
    }
