package common.storage;

import api.models.CreateUserRequest;
import api.requests.steps.UserAPISteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionAPIStorage {
    private static final ThreadLocal<SessionAPIStorage> INSTANCE = ThreadLocal.withInitial(SessionAPIStorage::new);

    private final LinkedHashMap<CreateUserRequest, UserAPISteps> usersStepsMap = new LinkedHashMap<>();

    private SessionAPIStorage() {
    }

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user : users) {
            INSTANCE.get().usersStepsMap.put(user, new UserAPISteps((user.getUsername()), user.getPassword()));
        }
    }

    /**
    Возвращаем объект CreateUserRequest по его порядковому номеру в списке созданных пользователей
    @param number Порядковый номер, начиная с 1 (а не с 0).
    @return Объект CreateUserRequest, соответствующий указанному порядковому номеру
    */

    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.get().usersStepsMap.keySet()).get(number-1);
    }

    public static CreateUserRequest getUser() {
        return getUser(1);
    }

    public static UserAPISteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.get().usersStepsMap.values()).get(number-1);
    }

    public static UserAPISteps getSteps() {
        return getSteps(1);
    }

    public static void clear() {
        INSTANCE.get().usersStepsMap.clear();
    }
}
