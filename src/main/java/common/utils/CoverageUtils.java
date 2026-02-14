package common.utils;

import com.github.viclovsky.swagger.coverage.FileSystemOutputWriter;
import com.github.viclovsky.swagger.coverage.SwaggerCoverageRestAssured;
import io.restassured.specification.RequestSpecification;

import java.nio.file.Paths;

public class CoverageUtils {
    public static RequestSpecification withCoverage(RequestSpecification baseSpec, String testCaseName) {
        return baseSpec
                .filter(new SwaggerCoverageRestAssured(
                        new FileSystemOutputWriter(Paths.get("target/swagger-coverage-output"))
                ))
                .header("X-Test-Case", testCaseName);
    }
}
