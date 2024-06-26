package co.com.bancolombia.utils;

import co.com.bancolombia.Constants;
import co.com.bancolombia.exceptions.CleanException;
import co.com.bancolombia.exceptions.ParamNotFoundException;
import com.github.mustachejava.resolver.DefaultResolver;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// @formatter:off
class UtilsTest {

    @Test
    void getVersionPlugin() {
        assertEquals(Constants.PLUGIN_VERSION, Utils.getVersionPlugin());
    }

    @Test
    void capitalize() {
        String test1 = "capitalize";
        String test2 = "capitalizeTest";

        assertEquals("Capitalize", Utils.capitalize(test1));
        assertEquals("CapitalizeTest", Utils.capitalize(test2));
    }

    @Test
    void decapitalize() {
        String test1 = "Decapitalize";
        String test2 = "DecapitalizeTest";
        String test3 = "DECAPITALIZE";
        assertEquals("decapitalize", Utils.decapitalize(test1));
        assertEquals("decapitalizeTest", Utils.decapitalize(test2));
        assertEquals("dECAPITALIZE", Utils.decapitalize(test3));
    }

    @Test
    void shouldJoinPath() {
        String expected = "a/b/c/d";
        String result = Utils.joinPath("a", "b", "c", "d");
        assertEquals(expected, result);
    }

    @Test
    void shouldReplacePlaceholders() throws CleanException {
        // Arrange
        String fillablePath = "default/driven-adapters/{{name}}/src/main/{{className}}";
        // Act
        Map<String, Object> params = new HashMap<>();
        params.put("name", "redis");
        params.put("className", "Redis.java");
        String result = Utils.fillPath(fillablePath, params);
        // Assert
        assertEquals("default/driven-adapters/redis/src/main/Redis.java", result);
    }

    // Assert
    @Test
    void shouldHandleErrorWhenNotParamReplacePlaceholders() {
        // Arrange
        String fillablePath = "default/driven-adapters/{{name}}/src/main/{{className}}";
        Map<String, Object> params = new HashMap<>();
        params.put("className", "Redis.java");
        // Act
        assertThrows(ParamNotFoundException.class, () -> Utils.fillPath(fillablePath, params));
    }

    @Test
    void shouldExtractDir() {
        // Arrange
        String classPath = "default/driven-adapters/package/src/main/Model.java";
        // Act
        String result = Utils.extractDir(classPath);
        // Assert
        assertEquals("default/driven-adapters/package/src/main", result);
    }

    @Test
    void shouldExtractDirWhenNoPath() {
        // Arrange
        String classPath = "Model.java";
        // Act
        String result = Utils.extractDir(classPath);
        // Assert
        assertNull(result);
    }

    @Test
    void shouldFormatTaskOptions() {
        // Arrange
        List<?> options = Arrays.asList(Options.values());
        // Act
        String result = Utils.formatTaskOptions(options);
        // Assert
        assertEquals("[A|BC|D]", result);
    }

    @Test
    void shouldFormatTaskOptionsSingle() {
        // Arrange
        List<?> options = Collections.singletonList("A");
        // Act
        String result = Utils.formatTaskOptions(options);
        // Assert
        assertEquals("[A]", result);
    }

    @Test
    void shouldAddDependency() {
        // Arrange
        String build = """
                apply plugin: 'org.springframework.boot'
                dependencies {
                    implementation project(':model')
                    implementation project(':usecase')
                    implementation 'org.springframework.boot:spring-boot-starter'
                }
                """;
        String expected = """
                apply plugin: 'org.springframework.boot'
                dependencies {
                \timplementation project(':my-module')
                    implementation project(':model')
                    implementation project(':usecase')
                    implementation 'org.springframework.boot:spring-boot-starter'
                }
                """;
        // Act
        String result = Utils.addDependency(build, "implementation project(':my-module')");
        // Assert
        assertEquals(expected, result);
    }

    @Test
    void shouldNotAddDependencyWhenExists() {
        // Arrange
        String build = """
                apply plugin: 'org.springframework.boot'
                dependencies {
                    implementation project(':my-module')
                    implementation project(':model')
                    implementation project(':usecase')
                    implementation 'org.springframework.boot:spring-boot-starter'
                }
                """;
        String expected = """
                apply plugin: 'org.springframework.boot'
                dependencies {
                    implementation project(':my-module')
                    implementation project(':model')
                    implementation project(':usecase')
                    implementation 'org.springframework.boot:spring-boot-starter'
                }
                """;
        // Act
        String result = Utils.addDependency(build, "implementation project(':my-module')");
        // Assert
        assertEquals(expected, result);
    }

    @Test
    void shouldAddConfigurationAndConfigurationsBlock() {
        // Arrange
        String build = """
                apply plugin: 'org.springframework.boot'

                dependencies {
                    implementation project(':model')
                    implementation project(':usecase')
                    implementation 'org.springframework.boot:spring-boot-starter'
                }""";
        String expected = """
                apply plugin: 'org.springframework.boot'

                dependencies {
                    implementation project(':model')
                    implementation project(':usecase')
                    implementation 'org.springframework.boot:spring-boot-starter'
                }

                configurations{
                \tcompile.exclude group: "org.springframework.boot", module:"spring-boot-starter-tomcat"
                }""";
        // Act
        String result =
                Utils.addConfiguration(
                        build,
                        "compile.exclude group: \"org.springframework.boot\", module:\"spring-boot-starter-tomcat\"");
        // Assert
        assertEquals(expected, result);
    }

    @Test
    void shouldAddConfiguration() {
        // Arrange
        String build = """
                apply plugin: 'org.springframework.boot'

                dependencies {
                    implementation project(':model')
                    implementation project(':usecase')
                    implementation 'org.springframework.boot:spring-boot-starter'
                }

                configurations {
                    compile.exclude group: "org.springframework.boot", module: "spring-boot-starter-tomcat"
                }""";
        String expected = """
                apply plugin: 'org.springframework.boot'

                dependencies {
                    implementation project(':model')
                    implementation project(':usecase')
                    implementation 'org.springframework.boot:spring-boot-starter'
                }

                configurations {
                \tcompile.exclude group: "co.com.bancolombia", module: "excluded-module"
                    compile.exclude group: "org.springframework.boot", module: "spring-boot-starter-tomcat"
                }""";
        // Act
        String result =
                Utils.addConfiguration(
                        build, "compile.exclude group: \"co.com.bancolombia\", module: \"excluded-module\"");
        // Assert
        assertEquals(expected, result);
    }

    @Test
    void shouldNotAddConfigurationWhenExists() {
        // Arrange
        String build = """
                apply plugin: 'org.springframework.boot'

                dependencies {
                    implementation project(':model')
                    implementation project(':usecase')
                    implementation 'org.springframework.boot:spring-boot-starter'
                }

                configurations {
                    compile.exclude group: "org.springframework.boot", module: "spring-boot-starter-tomcat"
                }""";
        String expected = """
                apply plugin: 'org.springframework.boot'

                dependencies {
                    implementation project(':model')
                    implementation project(':usecase')
                    implementation 'org.springframework.boot:spring-boot-starter'
                }

                configurations {
                    compile.exclude group: "org.springframework.boot", module: "spring-boot-starter-tomcat"
                }""";
        // Act
        String result =
                Utils.addConfiguration(
                        build,
                        "compile.exclude group: \"org.springframework.boot\", module: \"spring-boot-starter-tomcat\"");
        // Assert
        assertEquals(expected, result);
    }

    @Test
    void shouldGenerateDashName() {
        String res = Utils.toDashName("MyCamelCase");
        assertEquals("my-camel-case", res);
    }

    @Test
    void shouldGenerateDashNameNonStartsWithUpper() {
        String res = Utils.toDashName("myCamelCase");
        assertEquals("my-camel-case", res);
    }

    @Test
    void shouldAddModule() {
        // Arrange
        String settings = """
                rootProject.name = 'cleanArchitecture'

                include ':app-service'
                include ':model'
                include ':usecase'
                project(':app-service').projectDir = file('./applications/app-service')
                project(':model').projectDir = file('./domain/model')
                project(':usecase').projectDir = file('./domain/usecase')
                include ':api-rest'
                project(':api-rest').projectDir = file('./infrastructure/entry-points/api-rest')
                """;
        String settingsNew = """
                rootProject.name = 'cleanArchitecture'

                include ':app-service'
                include ':model'
                include ':usecase'
                project(':app-service').projectDir = file('./applications/app-service')
                project(':model').projectDir = file('./domain/model')
                project(':usecase').projectDir = file('./domain/usecase')
                include ':api-rest'
                project(':api-rest').projectDir = file('./infrastructure/entry-points/api-rest')
                                
                include ':my-module'
                project(':my-module').projectDir = file('./infrastructure/entry-points/my-module')""";
        // Act
        String result =
                Utils.addModule(
                        settings, Utils.INCLUDE_MODULE_JAVA, "my-module", "infrastructure/entry-points");
        // Assert
        assertEquals(settingsNew, result);
    }

    @Test
    void shouldNotAddRepeatedModule() {
        // Arrange
        String settings = """
                rootProject.name = 'cleanArchitecture'

                include ':app-service'
                include ':model'
                include ':usecase'
                project(':app-service').projectDir = file('./applications/app-service')
                project(':model').projectDir = file('./domain/model')
                project(':usecase').projectDir = file('./domain/usecase')
                include ':api-rest'
                project(':api-rest').projectDir = file('./infrastructure/entry-points/api-rest')
                include ':my-module'
                project(':my-module').projectDir = file('./infrastructure/entry-points/my-module')""";
        // Act
        String result =
                Utils.addModule(
                        settings, Utils.INCLUDE_MODULE_JAVA, "my-module", "infrastructure/entry-points");
        // Assert
        assertEquals(settings, result);
    }

    @Test
    void shouldRemoveModule() {
        // Arrange
        String settings = """
                rootProject.name = 'cleanArchitecture'

                include ':app-service'
                include ':model'
                include ':usecase'
                project(':app-service').projectDir = file('./applications/app-service')
                project(':model').projectDir = file('./domain/model')
                project(':usecase').projectDir = file('./domain/usecase')
                include ':api-rest'
                project(':api-rest').projectDir = file('./infrastructure/entry-points/api-rest')
                include ':my-module'
                project(':my-module').projectDir = file('./infrastructure/entry-points/my-module')""";
        String settingsExpected = """
                rootProject.name = 'cleanArchitecture'

                include ':app-service'
                include ':model'
                include ':usecase'
                project(':app-service').projectDir = file('./applications/app-service')
                project(':model').projectDir = file('./domain/model')
                project(':usecase').projectDir = file('./domain/usecase')
                include ':my-module'
                project(':my-module').projectDir = file('./infrastructure/entry-points/my-module')""";
        // Act
        String result = Utils.removeLinesIncludes(settings, "api-rest");
        // Assert
        assertEquals(settingsExpected, result);
    }

    @Test
    void ShouldGetBuildImplementation() {
        String result = Utils.buildImplementation("module");
        assertEquals("implementation 'module'", result);
    }

    @Test
    void ShouldGetBuildImplementationFromProject() {
        String result = Utils.buildImplementationFromProject(":module");
        assertEquals("implementation project(':module')", result);
    }

    @Test
    void shouldReplaceExpression() {
        String expected = "'com.fasterxml.jackson.core:jackson-databind:2.11.0'";
        String result =
                Utils.replaceExpression(
                        "'com.github.spullara.mustache.java:compiler:0.9.6'",
                        "['\"]com.github.spullara.mustache.java:compiler:[^\\$].+",
                        expected);
        assertEquals(expected, result);
    }

    @Test
    void shouldGetAllFilesWithGradleExtension() throws IOException {
        List<String> result = Utils.getAllFilesWithGradleExtension(".");
        assertEquals(true, result.stream().allMatch(s -> s.endsWith("gradle")));
    }

    @Test
    void sample() {
        assertEquals("my/path/package", "my.path.package".replace('.', '/'));
        assertEquals("mypathpackage", "my'path'package".replace("'", ""));
        assertEquals("mypathpackage", "my\"path\"package".replace("\"", ""));
    }

    @Test
    void shouldReplaceGroup() throws IOException {
        String regex =
                "(Get|Post|Put|Delete|Patch|Request)Mapping\\((.*(path|value)\\s*=\\s*|)\\\".*(/)\\\".*\\)";
        DefaultResolver resolver = new DefaultResolver();
        String text = FileUtils.getResourceAsString(resolver, "rest/mapping-before.txt");
        String expectedText = FileUtils.getResourceAsString(resolver, "rest/mapping-after.txt");

        String result = Utils.replaceGroup(text, regex, "", 4);
        assertEquals(expectedText, result);
    }

    private enum Options {
        A,
        BC,
        D
    }
}
// @formatter:on