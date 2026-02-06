package common.extensions;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private ConcurrentMap<String, Long> startTimes = new ConcurrentHashMap<>();

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        String testName = extensionContext.getRequiredTestClass().getPackageName() + "." + extensionContext.getDisplayName();
        startTimes.put(testName, System.currentTimeMillis());
        System.out.println("Thread " + Thread.currentThread().getName() + ": Test started " + testName);
    }


    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        String testName = extensionContext.getRequiredTestClass().getPackageName() + "." + extensionContext.getDisplayName();
        Long startTime = startTimes.get(testName);

        //проверка на null
        if (startTime == null) {
            System.err.println("Warning: No start time found for test: " + testName);
            return;
        }

        Long testDuration = System.currentTimeMillis() - startTime;
        System.out.println("Thread " + Thread.currentThread().getName() + ": Test finished " + testName + ", test duration " + testDuration + " ms");
    }
}
