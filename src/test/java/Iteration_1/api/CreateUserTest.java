package Iteration_1.api;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.DataBaseSteps;
import common.extensions.ApiVersionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.sql.*;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;
@ExtendWith(ApiVersionExtension.class)
public class CreateUserTest extends BaseTest {
    @Test
    public void adminCanCreateUserWithCorrectData() throws InterruptedException {
        CreateUserRequest createUserRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>
                (Endpoint.ADMIN_USER,
                        RequestSpecs.adminSpec(),
                        ResponseSpecs.entityWasCreated(),
                        "adminCanCreateUserWithCorrectData")
                .post(createUserRequest);

        ModelAssertions.assertThatModels(createUserRequest,createUserResponse).match();

//        //UserDao userDao = DataBaseSteps.getUserByUsername(createUserRequest.getUsername());
//        //DaoAndModelAssertions.assertThat(createUserResponse, userDao).match();
//
//        Thread.sleep(1000);
//
//
//        // === ДИАГНОСТИКА ===
//        System.out.println("\n=== ДИАГНОСТИКА ===");
//        System.out.println("Создан пользователь: " + createUserRequest.getUsername());
//        System.out.println("ID из ответа API: " + createUserResponse.getId());
//
//        // Проверяем прямое подключение к БД
//        try (Connection conn = DriverManager.getConnection(
//                "jdbc:postgresql://localhost:5433/nbank",
//                "postgres", "postgres")) {
//
//            // Проверяем таблицу
//            DatabaseMetaData meta = conn.getMetaData();
//            ResultSet tables = meta.getTables(null, "public", "customers", null);
//
//            if (!tables.next()) {
//                System.out.println("❌ Таблица 'customers' не найдена в БД!");
//                return; // Не продолжаем тест
//            }
//
//            // Ищем пользователя
//            PreparedStatement stmt = conn.prepareStatement(
//                    "SELECT * FROM customers WHERE username = ?"
//            );
//            stmt.setString(1, createUserRequest.getUsername());
//
//            ResultSet rs = stmt.executeQuery();
//            if (rs.next()) {
//                System.out.println("✅ Пользователь найден в БД!");
//                System.out.println("   ID в БД: " + rs.getLong("id"));
//                System.out.println("   Username: " + rs.getString("username"));
//                System.out.println("   Role: " + rs.getString("role"));
//
//                // Теперь проверяем через DataBaseSteps
//                UserDao userDao = DataBaseSteps.getUserByUsername(createUserRequest.getUsername());
//                if (userDao == null) {
//                    System.out.println("⚠️ DataBaseSteps не нашел пользователя!");
//                    System.out.println("   Проблема в DataBaseSteps!");
//                } else {
//                    System.out.println("✅ DataBaseSteps тоже нашел пользователя");
//                    DaoAndModelAssertions.assertThat(createUserResponse, userDao).match();
//                }
//            } else {
//                System.out.println("❌ Пользователь НЕ найден в БД!");
//                System.out.println("   Backend не сохранил данные в PostgreSQL");
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Ошибка при проверке БД: " + e.getMessage());
//        }
    }


    //@CsvSource({
//username field validation
//            "  , Password33$, USER, qqq"
//    })
    public static Stream<Arguments> userInvalidData() {
        //Username field validation
        return Stream.of(Arguments.of("  ", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots",
                        "Username cannot be blank", "Username must be between 3 and 15 characters")),
                Arguments.of("ab", "Password33$", "USER", "username", List.of("Username must be between 3 and 15 characters"),
                Arguments.of("abc$", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("abc%", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots")))));
    }


    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidData(String username, String password, String role, String errorKey, List<String> errorValues) {
        String testCaseId = String.format("adminCanNotCreateUserWithInvalidData_%s_%s_%s", username.trim(), password, role);
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new CrudRequester(Endpoint.ADMIN_USER, RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValues), testCaseId)
                .post(createUserRequest);

        assertNull(DataBaseSteps.getUserByUsername(createUserRequest.getUsername()));
    }
}

