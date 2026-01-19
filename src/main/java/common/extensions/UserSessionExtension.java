package common.extensions;

import api.models.CreateUserRequest;
import api.requests.steps.AdminAPISteps;
import common.annotations.UserSession;
import common.storage.SessionAPIStorage;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

import java.util.LinkedList;
import java.util.List;

public class UserSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        //Шаг1 : Проверка, что у теста есть аннотация UserSession
        UserSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {
            int userCount = annotation.value();

            SessionAPIStorage.clear();

            List<CreateUserRequest> users = new LinkedList<>();

            for (int i = 0; i < userCount; i++) {
                CreateUserRequest user = AdminAPISteps.createUser();
                users.add(user);
            }

            SessionAPIStorage.addUsers(users);

            int authAsUser = annotation.auth();

            BasePage.authAsUser(SessionAPIStorage.getUser(authAsUser));
        }
    }
}
