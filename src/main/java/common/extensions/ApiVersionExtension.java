package common.extensions;

import common.annotations.APIVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ApiVersionExtension implements ExecutionCondition {

    // Имя системной переменной/свойства для текущей версии API
    private static final String API_VERSION_PROPERTY = "api.version";

    // Версия по умолчанию (если не указана)
    private static final String DEFAULT_API_VERSION = "with_database_with_fix";

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        // 1. Получаем текущую версию API (из системных свойств или переменных окружения)
        String currentApiVersion = getCurrentApiVersion();

        // 2. Ищем аннотацию @APIVersion у теста
        APIVersion apiVersionAnnotation = findApiVersionAnnotation(context);

        // 3. Если аннотации нет - тест выполняется всегда
        if (apiVersionAnnotation == null) {
            return ConditionEvaluationResult.enabled(
                    "Тест не имеет ограничений по версии API (@APIVersion не указана)"
            );
        }

        // 4. Проверяем совместимость текущей версии с требуемыми
        String[] requiredVersions = apiVersionAnnotation.value();
        boolean isCompatible = isVersionCompatible(currentApiVersion, requiredVersions);

        // 5. Возвращаем результат
        if (isCompatible) {
            return ConditionEvaluationResult.enabled(
                    String.format("Тест совместим с текущей версией API: %s", currentApiVersion)
            );
        } else {
            return ConditionEvaluationResult.disabled(
                    String.format(
                            "Тест требует версию API: %s, но запущен на версии: %s",
                            String.join(", ", requiredVersions),
                            currentApiVersion
                    )
            );
        }
    }

    /**
     * Получает текущую версию API из:
     * 1. Системного свойства (-Dapi.version=...)
     * 2. Переменной окружения
     * 3. Значения по умолчанию
     */
    private String getCurrentApiVersion() {
        // Сначала проверяем системное свойство
        String version = System.getProperty(API_VERSION_PROPERTY);

        // Если не задано - проверяем переменную окружения
        if (version == null || version.isEmpty()) {
            version = System.getenv("API_VERSION");
        }

        // Если все еще не задано - используем значение по умолчанию
        if (version == null || version.isEmpty()) {
            version = DEFAULT_API_VERSION;
        }

        return version;
    }

    /**
     * Ищет аннотацию @APIVersion в тесте
     * Сначала проверяет метод, потом класс
     */
    private APIVersion findApiVersionAnnotation(ExtensionContext context) {
        // Проверяем аннотацию на методе теста
        Method testMethod = context.getTestMethod().orElse(null);
        if (testMethod != null) {
            APIVersion methodAnnotation = testMethod.getAnnotation(APIVersion.class);
            if (methodAnnotation != null) {
                return methodAnnotation;
            }
        }

        // Проверяем аннотацию на классе теста
        Class<?> testClass = context.getTestClass().orElse(null);
        if (testClass != null) {
            // Проверяем текущий класс и всех родителей (благодаря @Inherited)
            Class<?> clazz = testClass;
            while (clazz != null && clazz != Object.class) {
                APIVersion classAnnotation = clazz.getAnnotation(APIVersion.class);
                if (classAnnotation != null) {
                    return classAnnotation;
                }
                clazz = clazz.getSuperclass();
            }
        }

        return null;
    }

    /**
     * Проверяет совместимость версий
     */
    private boolean isVersionCompatible(String currentVersion, String[] requiredVersions) {
        return Arrays.stream(requiredVersions)
                .anyMatch(requiredVersion -> requiredVersion.equalsIgnoreCase(currentVersion));
    }
}
