/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5Suite.java to edit this template
 */

package coppercore.parameter_tools.test;

import coppercore.parameter_tools.json.JSONHandler;
import coppercore.parameter_tools.path_provider.EnvironmentHandler;
import coppercore.parameter_tools.path_provider.EnvironmentPathProvider;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author avidraccoon
 */
public class PathProviderTests {

    private static UnitTestingPathProvider pathProvider;
    private static PathProviderTestData blank;
    private JSONHandler jsonHandler;
    private EnvironmentHandler environmentHandler;
    private EnvironmentPathProvider testPathProvider;

    @BeforeAll
    public static void TestPrep() {
        pathProvider = new UnitTestingPathProvider().getDirectory("PathProviderTests");
        blank = new PathProviderTestData();
    }

    @BeforeEach
    public void CleanTest() {
        environmentHandler =
                EnvironmentHandler.getEnvironmentHandler(pathProvider.resolvePath("config.json"));
        testPathProvider = environmentHandler.getEnvironmentPathProvider();
        settupJsonHandler();
    }

    public void settupJsonHandler() {
        jsonHandler = new JSONHandler(environmentHandler.getEnvironmentPathProvider());
    }

    @Test
    public void getReadPathNormalBehaviorTest() {
        PathProviderTestData data = jsonHandler.getObject(blank, "NormalFile.json");
        Assertions.assertEquals("Test Value 1", data.getTestValue());
    }

    @Test
    public void getReadPathDefualtingBehaviorTest() {
        PathProviderTestData data = jsonHandler.getObject(blank, "DefaultFile.json");
        Assertions.assertEquals("Test Value 2", data.getTestValue());
    }

    @Test
    public void getReadPathMissingBehaviorTest() {
        try {
            jsonHandler.getObject(blank, "RandomNoneExistantFile.json");
            Assertions.fail("Should throw error about file missing");
        } catch (Exception e) {
            Assertions.assertEquals(
                    "RandomNoneExistantFile.json does not exist at "
                            + pathProvider.getFullPath()
                            + File.separator
                            + "defaults"
                            + File.separator
                            + "RandomNoneExistantFile.json",
                    e.getMessage());
        }
    }

    @Test
    public void getWritePathNormalBehaviorTest() {
        Assertions.assertEquals(
                pathProvider.getFullPath()
                        + File.separator
                        + "normal"
                        + File.separator
                        + "NormalFile.json",
                testPathProvider.resolveWritePath("NormalFile.json"));
    }

    @Test
    public void getWritePathDefualtingBehaviorTest() {
        Assertions.assertEquals(
                pathProvider.getFullPath()
                        + File.separator
                        + "defaults"
                        + File.separator
                        + "DefaultFile.json",
                testPathProvider.resolveWritePath("DefaultFile.json"));
    }

    @Test
    public void getWritePathDefualtingMissingTest() {
        Assertions.assertEquals(
                pathProvider.getFullPath()
                        + File.separator
                        + "defaults"
                        + File.separator
                        + "RandomNoneExistantFile.json",
                testPathProvider.resolveWritePath("RandomNoneExistantFile.json"));
    }

    @Test
    public void customPathTest() {
        environmentHandler = environmentHandler.setEnvironment("custom_path");
        testPathProvider = environmentHandler.getEnvironmentPathProvider();
        Assertions.assertEquals(
                pathProvider.getFullPath()
                        + File.separator
                        + "custom"
                        + File.separator
                        + "CustomFile.json",
                testPathProvider.resolveWritePath("CustomFile.json"));
    }
}
